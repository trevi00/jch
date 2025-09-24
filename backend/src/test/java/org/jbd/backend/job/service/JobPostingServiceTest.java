package org.jbd.backend.job.service;

import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.domain.enums.ExperienceLevel;
import org.jbd.backend.job.domain.enums.JobStatus;
import org.jbd.backend.job.domain.enums.JobType;
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
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobPostingService 테스트")
class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    private User companyUser;
    private JobPosting jobPosting;

    @BeforeEach
    void setUp() {
        companyUser = new User("company@test.com", "password", "테스트기업", UserType.GENERAL);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(companyUser, 1L);
        } catch (Exception e) {
            // Handle reflection exception
        }

        jobPosting = new JobPosting(companyUser, "백엔드 개발자", "테스트기업", 
                                   "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
    }

    @Test
    @DisplayName("채용공고를 생성할 수 있다")
    void canCreateJobPosting() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(companyUser));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(jobPosting);

        // When
        JobPosting created = jobPostingService.createJobPosting(
            1L, "백엔드 개발자", "테스트기업", "서울", 
            JobType.FULL_TIME, ExperienceLevel.MID_LEVEL, 
            "Spring Boot 개발자를 모집합니다.", 
            50000000, 60000000
        );

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("백엔드 개발자");
        verify(jobPostingRepository).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 채용공고 생성 시 예외가 발생한다")
    void throwsExceptionWhenUserNotFound() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> 
            jobPostingService.createJobPosting(
                999L, "백엔드 개발자", "테스트기업", "서울", 
                JobType.FULL_TIME, ExperienceLevel.MID_LEVEL, 
                "설명", 50000000, 60000000
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("채용공고를 발행할 수 있다")
    void canPublishJobPosting() {
        // Given
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(jobPosting));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(jobPosting);
        LocalDate deadlineDate = LocalDate.now().plusDays(30);

        // When
        JobPosting published = jobPostingService.publishJobPosting(1L, deadlineDate);

        // Then
        assertThat(published.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        assertThat(published.getDeadlineDate()).isEqualTo(deadlineDate);
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    @DisplayName("채용공고를 조회할 수 있다")
    void canGetJobPosting() {
        // Given
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(jobPosting));

        // When
        JobPosting found = jobPostingService.getJobPosting(1L);

        // Then
        assertThat(found).isEqualTo(jobPosting);
    }

    @Test
    @DisplayName("존재하지 않는 채용공고 조회 시 예외가 발생한다")
    void throwsExceptionWhenJobPostingNotFound() {
        // Given
        given(jobPostingRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> jobPostingService.getJobPosting(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("채용공고를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("발행된 채용공고 목록을 조회할 수 있다")
    void canGetPublishedJobPostings() {
        // Given
        JobPosting publishedJob1 = new JobPosting(companyUser, "백엔드 개발자", "회사1", 
                                                 "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        JobPosting publishedJob2 = new JobPosting(companyUser, "프론트엔드 개발자", "회사2", 
                                                 "부산", JobType.PART_TIME, ExperienceLevel.JUNIOR);
        
        publishedJob1.publish(LocalDate.now().plusDays(30));
        publishedJob2.publish(LocalDate.now().plusDays(20));

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<JobPosting> jobPage = new PageImpl<>(Arrays.asList(publishedJob1, publishedJob2));
        
        given(jobPostingRepository.findByStatus(JobStatus.PUBLISHED, pageRequest)).willReturn(jobPage);

        // When
        Page<JobPosting> result = jobPostingService.getPublishedJobPostings(pageRequest);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(job -> job.getStatus() == JobStatus.PUBLISHED);
    }

    @Test
    @DisplayName("지역별 채용공고를 조회할 수 있다")
    void canSearchJobPostingsByLocation() {
        // Given
        JobPosting seoulJob = new JobPosting(companyUser, "서울 개발자", "회사1", 
                                            "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        List<JobPosting> seoulJobs = Arrays.asList(seoulJob);
        
        given(jobPostingRepository.findByLocation("서울")).willReturn(seoulJobs);

        // When
        List<JobPosting> result = jobPostingService.searchJobPostingsByLocation("서울");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocation()).isEqualTo("서울");
    }

    @Test
    @DisplayName("직무 유형별 채용공고를 조회할 수 있다")
    void canSearchJobPostingsByJobType() {
        // Given
        JobPosting fullTimeJob = new JobPosting(companyUser, "정규직 개발자", "회사1", 
                                               "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        List<JobPosting> fullTimeJobs = Arrays.asList(fullTimeJob);
        
        given(jobPostingRepository.findByJobType(JobType.FULL_TIME)).willReturn(fullTimeJobs);

        // When
        List<JobPosting> result = jobPostingService.searchJobPostingsByJobType(JobType.FULL_TIME);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJobType()).isEqualTo(JobType.FULL_TIME);
    }

    @Test
    @DisplayName("경력 수준별 채용공고를 조회할 수 있다")
    void canSearchJobPostingsByExperienceLevel() {
        // Given
        JobPosting midLevelJob = new JobPosting(companyUser, "중급 개발자", "회사1", 
                                               "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        List<JobPosting> midLevelJobs = Arrays.asList(midLevelJob);
        
        given(jobPostingRepository.findByExperienceLevel(ExperienceLevel.MID_LEVEL)).willReturn(midLevelJobs);

        // When
        List<JobPosting> result = jobPostingService.searchJobPostingsByExperienceLevel(ExperienceLevel.MID_LEVEL);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExperienceLevel()).isEqualTo(ExperienceLevel.MID_LEVEL);
    }

    @Test
    @DisplayName("키워드로 채용공고를 검색할 수 있다")
    void canSearchJobPostingsByKeyword() {
        // Given
        JobPosting springJob = new JobPosting(companyUser, "Spring 개발자", "회사1", 
                                             "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        springJob.updateContent("Spring Boot를 활용한 백엔드 개발", null, null, null);
        
        List<JobPosting> springJobs = Arrays.asList(springJob);
        
        given(jobPostingRepository.findByTitleContainingOrDescriptionContaining("Spring"))
            .willReturn(springJobs);

        // When
        List<JobPosting> result = jobPostingService.searchJobPostingsByKeyword("Spring");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Spring");
    }

    @Test
    @DisplayName("채용공고 조회수를 증가시킬 수 있다")
    void canIncrementViewCount() {
        // Given
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(jobPosting));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(jobPosting);
        Long originalViewCount = jobPosting.getViewCount();

        // When
        jobPostingService.incrementViewCount(1L);

        // Then
        verify(jobPostingRepository).save(jobPosting);
        assertThat(jobPosting.getViewCount()).isEqualTo(originalViewCount + 1);
    }

    @Test
    @DisplayName("기업 사용자의 채용공고 목록을 조회할 수 있다")
    void canGetJobPostingsByCompanyUser() {
        // Given
        JobPosting job1 = new JobPosting(companyUser, "백엔드 개발자", "테스트기업", 
                                        "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        JobPosting job2 = new JobPosting(companyUser, "프론트엔드 개발자", "테스트기업", 
                                        "서울", JobType.FULL_TIME, ExperienceLevel.JUNIOR);
        
        List<JobPosting> companyJobs = Arrays.asList(job1, job2);
        
        given(jobPostingRepository.findByCompanyUser(companyUser)).willReturn(companyJobs);

        // When
        List<JobPosting> result = jobPostingService.getJobPostingsByCompanyUser(companyUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(job -> job.getCompanyUser().equals(companyUser));
    }

    @Test
    @DisplayName("채용공고를 수정할 수 있다")
    void canUpdateJobPosting() {
        // Given
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(jobPosting));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(jobPosting);

        // When
        JobPosting updated = jobPostingService.updateJobPosting(
            1L, "시니어 백엔드 개발자", "새로운 설명", 
            60000000, 70000000
        );

        // Then
        assertThat(updated.getTitle()).isEqualTo("시니어 백엔드 개발자");
        assertThat(updated.getDescription()).isEqualTo("새로운 설명");
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    @DisplayName("채용공고를 마감할 수 있다")
    void canCloseJobPosting() {
        // Given
        jobPosting.publish(LocalDate.now().plusDays(30));
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(jobPosting));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(jobPosting);

        // When
        JobPosting closed = jobPostingService.closeJobPosting(1L);

        // Then
        assertThat(closed.getStatus()).isEqualTo(JobStatus.CLOSED);
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    @DisplayName("만료된 채용공고들을 조회할 수 있다")
    void canGetExpiredJobPostings() {
        // Given
        LocalDate today = LocalDate.now();
        JobPosting expiredJob = new JobPosting(companyUser, "만료된 공고", "회사1", 
                                              "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        
        List<JobPosting> expiredJobs = Arrays.asList(expiredJob);
        
        given(jobPostingRepository.findByStatusAndDeadlineDateBefore(JobStatus.PUBLISHED, today))
            .willReturn(expiredJobs);

        // When
        List<JobPosting> result = jobPostingService.getExpiredJobPostings();

        // Then
        assertThat(result).hasSize(1);
        verify(jobPostingRepository).findByStatusAndDeadlineDateBefore(JobStatus.PUBLISHED, today);
    }
}