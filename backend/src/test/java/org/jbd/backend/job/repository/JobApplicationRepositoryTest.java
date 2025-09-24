package org.jbd.backend.job.repository;

import org.jbd.backend.job.domain.JobApplication;
import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.domain.enums.ApplicationStatus;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("JobApplicationRepository 테스트")
class JobApplicationRepositoryTest {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    @Autowired
    private JobPostingRepository jobPostingRepository;
    
    @Autowired
    private UserRepository userRepository;

    private User jobSeeker;
    private User companyUser;
    private JobPosting jobPosting;

    @BeforeEach
    void setUp() {
        jobSeeker = new User("jobseeker@test.com", "password", "구직자", UserType.GENERAL);

        companyUser = new User("company@test.com", "password", "테스트기업", UserType.GENERAL);

        jobPosting = new JobPosting(companyUser, "백엔드 개발자", "테스트기업", 
                                   "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        jobPosting.publish(LocalDate.now().plusDays(30));

        jobSeeker = userRepository.save(jobSeeker);
        companyUser = userRepository.save(companyUser);
        jobPosting = jobPostingRepository.save(jobPosting);
    }

    @Test
    @DisplayName("지원서를 저장할 수 있다")
    void canSaveJobApplication() {
        // Given
        JobApplication application = new JobApplication(jobSeeker, jobPosting, "지원합니다!");

        // When
        JobApplication saved = jobApplicationRepository.save(application);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser()).isEqualTo(jobSeeker);
        assertThat(saved.getJobPosting()).isEqualTo(jobPosting);
        assertThat(saved.getCoverLetter()).isEqualTo("지원합니다!");
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
    }

    @Test
    @DisplayName("사용자의 지원서 목록을 조회할 수 있다")
    void canFindByUser() {
        // Given
        JobApplication application1 = new JobApplication(jobSeeker, jobPosting, "첫 번째 지원");
        
        JobPosting anotherJobPosting = new JobPosting(companyUser, "프론트엔드 개발자", "테스트기업2", 
                                                     "부산", JobType.PART_TIME, ExperienceLevel.JUNIOR);
        anotherJobPosting = jobPostingRepository.save(anotherJobPosting);
        
        JobApplication application2 = new JobApplication(jobSeeker, anotherJobPosting, "두 번째 지원");

        jobApplicationRepository.save(application1);
        jobApplicationRepository.save(application2);

        // When
        List<JobApplication> applications = jobApplicationRepository.findByUser(jobSeeker);

        // Then
        assertThat(applications).hasSize(2);
        assertThat(applications).allMatch(app -> app.getUser().equals(jobSeeker));
    }

    @Test
    @DisplayName("채용공고의 지원서 목록을 조회할 수 있다")
    void canFindByJobPosting() {
        // Given
        User anotherJobSeeker = new User("another@test.com", "password", "다른 구직자", UserType.GENERAL);
        anotherJobSeeker = userRepository.save(anotherJobSeeker);

        JobApplication application1 = new JobApplication(jobSeeker, jobPosting, "첫 번째 지원");
        JobApplication application2 = new JobApplication(anotherJobSeeker, jobPosting, "두 번째 지원");

        jobApplicationRepository.save(application1);
        jobApplicationRepository.save(application2);

        // When
        List<JobApplication> applications = jobApplicationRepository.findByJobPosting(jobPosting);

        // Then
        assertThat(applications).hasSize(2);
        assertThat(applications).allMatch(app -> app.getJobPosting().equals(jobPosting));
    }

    @Test
    @DisplayName("지원 상태별로 지원서를 조회할 수 있다")
    void canFindByStatus() {
        // Given
        JobApplication application1 = new JobApplication(jobSeeker, jobPosting, "지원1");

        User anotherJobSeeker = new User("another@test.com", "password", "다른 구직자", UserType.GENERAL);
        anotherJobSeeker = userRepository.save(anotherJobSeeker);

        JobApplication application2 = new JobApplication(anotherJobSeeker, jobPosting, "지원2");
        application2.review(); // REVIEWED 상태로 변경

        jobApplicationRepository.save(application1);
        jobApplicationRepository.save(application2);

        // When
        List<JobApplication> submittedApps = jobApplicationRepository.findByStatus(ApplicationStatus.SUBMITTED);
        List<JobApplication> reviewedApps = jobApplicationRepository.findByStatus(ApplicationStatus.REVIEWED);

        // Then
        assertThat(submittedApps).hasSize(1);
        assertThat(reviewedApps).hasSize(1);
    }

    @Test
    @DisplayName("사용자와 채용공고로 지원서를 조회할 수 있다")
    void canFindByUserAndJobPosting() {
        // Given
        JobApplication application = new JobApplication(jobSeeker, jobPosting, "지원합니다!");
        jobApplicationRepository.save(application);

        // When
        Optional<JobApplication> found = jobApplicationRepository.findByUserAndJobPosting(jobSeeker, jobPosting);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCoverLetter()).isEqualTo("지원합니다!");
    }

    @Test
    @DisplayName("존재하지 않는 지원서 조회 시 empty를 반환한다")
    void returnsEmptyWhenNotFound() {
        // Given
        User anotherUser = new User("another@test.com", "password", "다른 사용자", UserType.GENERAL);
        anotherUser = userRepository.save(anotherUser);

        // When
        Optional<JobApplication> found = jobApplicationRepository.findByUserAndJobPosting(anotherUser, jobPosting);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("채용공고별 지원서 개수를 조회할 수 있다")
    void canCountByJobPosting() {
        // Given
        User user1 = new User("user1@test.com", "password", "사용자1", UserType.GENERAL);

        User user2 = new User("user2@test.com", "password", "사용자2", UserType.GENERAL);

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        JobApplication application1 = new JobApplication(user1, jobPosting, "지원1");
        JobApplication application2 = new JobApplication(user2, jobPosting, "지원2");
        JobApplication application3 = new JobApplication(jobSeeker, jobPosting, "지원3");

        jobApplicationRepository.save(application1);
        jobApplicationRepository.save(application2);
        jobApplicationRepository.save(application3);

        // When
        long count = jobApplicationRepository.countByJobPosting(jobPosting);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("특정 기간 내 지원서를 조회할 수 있다")
    void canFindApplicationsInDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        JobPosting anotherJobPosting2 = new JobPosting(companyUser, "DevOps 엔지니어", "테스트기업3", 
                                                      "대전", JobType.FULL_TIME, ExperienceLevel.SENIOR);
        anotherJobPosting2 = jobPostingRepository.save(anotherJobPosting2);
        
        JobApplication recentApplication = new JobApplication(jobSeeker, jobPosting, "최근 지원");

        // 다른 공고에 대한 지원
        JobApplication oldApplication = new JobApplication(jobSeeker, anotherJobPosting2, "오래된 지원");
        jobApplicationRepository.save(recentApplication);
        jobApplicationRepository.save(oldApplication);

        // When
        List<JobApplication> applicationsInRange = 
            jobApplicationRepository.findByCreatedAtBetween(startDate, endDate);

        // Then
        assertThat(applicationsInRange).hasSize(2); // 두 지원서 모두 현재 시간으로 생성됨
    }

    @Test
    @DisplayName("채용공고와 지원 상태로 지원서를 페이징 조회할 수 있다")
    void canFindByJobPostingAndStatusWithPaging() {
        // Given
        for (int i = 1; i <= 15; i++) {
            User user = new User("user" + i + "@test.com", "password", "사용자" + i, UserType.GENERAL);
            user = userRepository.save(user);

            JobApplication application = new JobApplication(user, jobPosting, "지원서 " + i);
            if (i % 3 == 0) {
                application.review(); // 일부는 리뷰 상태로 변경
            }
            jobApplicationRepository.save(application);
        }

        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        // When
        Page<JobApplication> submittedApps = 
            jobApplicationRepository.findByJobPostingAndStatus(jobPosting, ApplicationStatus.SUBMITTED, pageRequest);

        // Then
        assertThat(submittedApps.getContent()).hasSize(5);
        assertThat(submittedApps.getTotalElements()).isEqualTo(10); // 15개 중 5개는 REVIEWED 상태
        assertThat(submittedApps.getTotalPages()).isEqualTo(2);
    }
}