package org.jbd.backend.job.service;

import org.jbd.backend.job.domain.JobApplication;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.domain.enums.ApplicationStatus;
import org.jbd.backend.job.domain.enums.ExperienceLevel;
import org.jbd.backend.job.domain.enums.JobStatus;
import org.jbd.backend.job.domain.enums.JobType;
import org.jbd.backend.job.repository.JobApplicationRepository;
import org.jbd.backend.job.repository.JobPostingRepository;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobApplicationService 테스트")
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private User jobSeeker;
    private User companyUser;
    private JobPosting jobPosting;
    private JobApplication jobApplication;

    @BeforeEach
    void setUp() {
        jobSeeker = new User("jobseeker@test.com", "password", "구직자", UserType.GENERAL);
        // Set ID using reflection for testing
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(jobSeeker, 1L);
        } catch (Exception e) {
            // Handle reflection exception
        }

        companyUser = new User("company@test.com", "password", "테스트기업", UserType.GENERAL);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(companyUser, 2L);
        } catch (Exception e) {
            // Handle reflection exception
        }

        jobPosting = new JobPosting(companyUser, "백엔드 개발자", "테스트기업", 
                                   "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        jobPosting.publish(LocalDate.now().plusDays(30));

        jobApplication = new JobApplication(jobSeeker, jobPosting, "지원합니다!");
    }

    @Test
    @DisplayName("채용공고에 지원할 수 있다")
    void canApplyToJobPosting() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(jobSeeker));
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(jobPosting));
        given(jobApplicationRepository.existsByUserAndJobPosting(jobSeeker, jobPosting)).willReturn(false);
        given(jobApplicationRepository.save(any(JobApplication.class))).willReturn(jobApplication);

        // When
        JobApplication created = jobApplicationService.applyToJobPosting(1L, 1L, "지원합니다!");

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getCoverLetter()).isEqualTo("지원합니다!");
        assertThat(created.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        verify(jobApplicationRepository).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 지원 시 예외가 발생한다")
    void throwsExceptionWhenUserNotFoundForApplication() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> 
            jobApplicationService.applyToJobPosting(999L, 1L, "지원합니다!")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 채용공고에 지원 시 예외가 발생한다")
    void throwsExceptionWhenJobPostingNotFoundForApplication() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(jobSeeker));
        given(jobPostingRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> 
            jobApplicationService.applyToJobPosting(1L, 999L, "지원합니다!")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("채용공고를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이미 지원한 채용공고에 중복 지원 시 예외가 발생한다")
    void throwsExceptionWhenAlreadyApplied() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(jobSeeker));
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(jobPosting));
        given(jobApplicationRepository.existsByUserAndJobPosting(jobSeeker, jobPosting)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> 
            jobApplicationService.applyToJobPosting(1L, 1L, "지원합니다!")
        ).isInstanceOf(IllegalStateException.class)
         .hasMessage("이미 지원한 채용공고입니다.");
    }

    @Test
    @DisplayName("마감된 채용공고에 지원 시 예외가 발생한다")
    void throwsExceptionWhenJobPostingIsClosed() {
        // Given
        jobPosting.close();
        given(userRepository.findById(1L)).willReturn(Optional.of(jobSeeker));
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(jobPosting));

        // When & Then
        assertThatThrownBy(() -> 
            jobApplicationService.applyToJobPosting(1L, 1L, "지원합니다!")
        ).isInstanceOf(IllegalStateException.class)
         .hasMessage("지원할 수 없는 채용공고입니다.");
    }

    @Test
    @DisplayName("지원서를 조회할 수 있다")
    void canGetJobApplication() {
        // Given
        given(jobApplicationRepository.findById(1L)).willReturn(Optional.of(jobApplication));

        // When
        JobApplication found = jobApplicationService.getJobApplication(1L);

        // Then
        assertThat(found).isEqualTo(jobApplication);
    }

    @Test
    @DisplayName("존재하지 않는 지원서 조회 시 예외가 발생한다")
    void throwsExceptionWhenJobApplicationNotFound() {
        // Given
        given(jobApplicationRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> jobApplicationService.getJobApplication(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("지원서를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자의 지원서 목록을 조회할 수 있다")
    void canGetJobApplicationsByUser() {
        // Given
        JobApplication app1 = new JobApplication(jobSeeker, jobPosting, "지원서1");
        JobApplication app2 = new JobApplication(jobSeeker, jobPosting, "지원서2");
        List<JobApplication> applications = Arrays.asList(app1, app2);
        
        given(jobApplicationRepository.findByUser(jobSeeker)).willReturn(applications);

        // When
        List<JobApplication> result = jobApplicationService.getJobApplicationsByUser(jobSeeker);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(app -> app.getUser().equals(jobSeeker));
    }

    @Test
    @DisplayName("채용공고의 지원서 목록을 조회할 수 있다")
    void canGetJobApplicationsByJobPosting() {
        // Given
        User anotherJobSeeker = new User("another@test.com", "password", "다른 구직자", UserType.GENERAL);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(anotherJobSeeker, 3L);
        } catch (Exception e) {
            // Handle reflection exception
        }

        JobApplication app1 = new JobApplication(jobSeeker, jobPosting, "지원서1");
        JobApplication app2 = new JobApplication(anotherJobSeeker, jobPosting, "지원서2");
        
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<JobApplication> applicationPage = new PageImpl<>(Arrays.asList(app1, app2));
        
        given(jobApplicationRepository.findByJobPosting(jobPosting, pageRequest)).willReturn(applicationPage);

        // When
        Page<JobApplication> result = jobApplicationService.getJobApplicationsByJobPosting(jobPosting, pageRequest);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(app -> app.getJobPosting().equals(jobPosting));
    }

    @Test
    @DisplayName("지원서를 심사 상태로 변경할 수 있다")
    void canReviewJobApplication() {
        // Given
        given(jobApplicationRepository.findById(1L)).willReturn(Optional.of(jobApplication));
        given(jobApplicationRepository.save(any(JobApplication.class))).willReturn(jobApplication);

        // When
        JobApplication reviewed = jobApplicationService.reviewJobApplication(1L);

        // Then
        assertThat(reviewed.getStatus()).isEqualTo(ApplicationStatus.REVIEWED);
        verify(jobApplicationRepository).save(jobApplication);
    }

    @Test
    @DisplayName("지원서를 서류 통과 상태로 변경할 수 있다")
    void canPassDocumentReview() {
        // Given
        jobApplication.review(); // 먼저 심사 상태로 변경
        given(jobApplicationRepository.findById(1L)).willReturn(Optional.of(jobApplication));
        given(jobApplicationRepository.save(any(JobApplication.class))).willReturn(jobApplication);

        // When
        JobApplication passed = jobApplicationService.passDocumentReview(1L);

        // Then
        assertThat(passed.getStatus()).isEqualTo(ApplicationStatus.DOCUMENT_PASSED);
        verify(jobApplicationRepository).save(jobApplication);
    }

    @Test
    @DisplayName("지원서를 면접 통과 상태로 변경할 수 있다")
    void canPassInterview() {
        // Given
        jobApplication.review();
        jobApplication.passDocumentReview();
        given(jobApplicationRepository.findById(1L)).willReturn(Optional.of(jobApplication));
        given(jobApplicationRepository.save(any(JobApplication.class))).willReturn(jobApplication);

        // When
        JobApplication passed = jobApplicationService.passInterview(1L);

        // Then
        assertThat(passed.getStatus()).isEqualTo(ApplicationStatus.INTERVIEW_PASSED);
        verify(jobApplicationRepository).save(jobApplication);
    }

    @Test
    @DisplayName("지원서를 채용 상태로 변경할 수 있다")
    void canHireApplicant() {
        // Given
        jobApplication.review();
        jobApplication.passDocumentReview();
        jobApplication.passInterview();
        given(jobApplicationRepository.findById(1L)).willReturn(Optional.of(jobApplication));
        given(jobApplicationRepository.save(any(JobApplication.class))).willReturn(jobApplication);

        // When
        JobApplication hired = jobApplicationService.hireApplicant(1L);

        // Then
        assertThat(hired.getStatus()).isEqualTo(ApplicationStatus.HIRED);
        verify(jobApplicationRepository).save(jobApplication);
    }

    @Test
    @DisplayName("지원서를 거부 상태로 변경할 수 있다")
    void canRejectJobApplication() {
        // Given
        given(jobApplicationRepository.findById(1L)).willReturn(Optional.of(jobApplication));
        given(jobApplicationRepository.save(any(JobApplication.class))).willReturn(jobApplication);

        // When
        JobApplication rejected = jobApplicationService.rejectJobApplication(1L, "요구사항과 맞지 않습니다.");

        // Then
        assertThat(rejected.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(rejected.getRejectionReason()).isEqualTo("요구사항과 맞지 않습니다.");
        verify(jobApplicationRepository).save(jobApplication);
    }

    @Test
    @DisplayName("특정 상태의 지원서들을 조회할 수 있다")
    void canGetJobApplicationsByStatus() {
        // Given
        JobApplication submittedApp = new JobApplication(jobSeeker, jobPosting, "제출된 지원서");
        List<JobApplication> submittedApps = Arrays.asList(submittedApp);
        
        given(jobApplicationRepository.findByStatus(ApplicationStatus.SUBMITTED)).willReturn(submittedApps);

        // When
        List<JobApplication> result = jobApplicationService.getJobApplicationsByStatus(ApplicationStatus.SUBMITTED);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).allMatch(app -> app.getStatus() == ApplicationStatus.SUBMITTED);
    }

    @Test
    @DisplayName("채용공고의 지원자 수를 조회할 수 있다")
    void canGetApplicationCountByJobPosting() {
        // Given
        given(jobApplicationRepository.countByJobPosting(jobPosting)).willReturn(5L);

        // When
        long count = jobApplicationService.getApplicationCountByJobPosting(jobPosting);

        // Then
        assertThat(count).isEqualTo(5L);
    }
}