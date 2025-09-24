package org.jbd.backend.job.repository;

import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.domain.enums.ExperienceLevel;
import org.jbd.backend.job.domain.enums.JobStatus;
import org.jbd.backend.job.domain.enums.JobType;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("채용공고 Repository 테스트")
class JobPostingRepositoryTest {

    @Autowired
    private JobPostingRepository jobPostingRepository;
    
    @Autowired
    private UserRepository userRepository;

    private User companyUser;

    @BeforeEach
    void setUp() {
        companyUser = new User("company@example.com", "password", "테스트기업", UserType.COMPANY);
        companyUser = userRepository.save(companyUser);
    }

    @Test
    @DisplayName("채용공고를 저장할 수 있다")
    void canSaveJobPosting() {
        // Given
        JobPosting jobPosting = new JobPosting(companyUser, "백엔드 개발자", "테스트기업", 
                                              "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        jobPosting.updateContent("Java/Spring 백엔드 개발", "CS 기초지식", "Java, Spring Boot", "복리후생 우수");

        // When
        JobPosting saved = jobPostingRepository.save(jobPosting);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("백엔드 개발자");
        assertThat(saved.getCompanyName()).isEqualTo("테스트기업");
        assertThat(saved.getLocation()).isEqualTo("서울");
        assertThat(saved.getJobType()).isEqualTo(JobType.FULL_TIME);
        assertThat(saved.getStatus()).isEqualTo(JobStatus.DRAFT);
    }

    @Test
    @DisplayName("게시된 채용공고를 조회할 수 있다")
    void canFindPublishedJobPostings() {
        // Given
        JobPosting published1 = new JobPosting(companyUser, "백엔드 개발자", "테스트기업", 
                                              "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        published1.publish(LocalDate.now().plusDays(30));
        
        JobPosting published2 = new JobPosting(companyUser, "프론트엔드 개발자", "테스트기업", 
                                              "부산", JobType.FULL_TIME, ExperienceLevel.JUNIOR);
        published2.publish(LocalDate.now().plusDays(20));
        
        JobPosting draft = new JobPosting(companyUser, "임시저장공고", "테스트기업", 
                                         "대구", JobType.PART_TIME, ExperienceLevel.ENTRY_LEVEL);
        
        jobPostingRepository.save(published1);
        jobPostingRepository.save(published2);
        jobPostingRepository.save(draft);

        // When
        List<JobPosting> publishedJobs = jobPostingRepository.findByStatus(JobStatus.PUBLISHED);

        // Then
        assertThat(publishedJobs).hasSize(2);
        assertThat(publishedJobs).extracting("title").containsExactlyInAnyOrder("프론트엔드 개발자", "백엔드 개발자");
    }

    @Test
    @DisplayName("지역별 채용공고를 조회할 수 있다")
    void canFindJobPostingsByLocation() {
        // Given
        JobPosting seoul1 = new JobPosting(companyUser, "서울 백엔드", "테스트기업", 
                                          "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        seoul1.publish(LocalDate.now().plusDays(30));
        
        JobPosting seoul2 = new JobPosting(companyUser, "서울 프론트엔드", "테스트기업", 
                                          "서울", JobType.FULL_TIME, ExperienceLevel.JUNIOR);
        seoul2.publish(LocalDate.now().plusDays(20));
        
        JobPosting busan = new JobPosting(companyUser, "부산 개발자", "테스트기업", 
                                         "부산", JobType.FULL_TIME, ExperienceLevel.SENIOR);
        busan.publish(LocalDate.now().plusDays(25));
        
        jobPostingRepository.save(seoul1);
        jobPostingRepository.save(seoul2);
        jobPostingRepository.save(busan);

        // When
        List<JobPosting> seoulJobs = jobPostingRepository.findByLocation("서울");

        // Then
        assertThat(seoulJobs).hasSize(2);
        assertThat(seoulJobs).hasSize(2);
        assertThat(seoulJobs).allMatch(job -> job.getLocation().equals("서울"));
    }

    @Test
    @DisplayName("채용형태별 채용공고를 조회할 수 있다")
    void canFindJobPostingsByJobType() {
        // Given
        JobPosting fullTime1 = new JobPosting(companyUser, "정규직1", "테스트기업", 
                                             "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        fullTime1.publish(LocalDate.now().plusDays(30));
        
        JobPosting fullTime2 = new JobPosting(companyUser, "정규직2", "테스트기업", 
                                             "부산", JobType.FULL_TIME, ExperienceLevel.JUNIOR);
        fullTime2.publish(LocalDate.now().plusDays(20));
        
        JobPosting partTime = new JobPosting(companyUser, "파트타임", "테스트기업", 
                                            "대구", JobType.PART_TIME, ExperienceLevel.ANY);
        partTime.publish(LocalDate.now().plusDays(25));
        
        jobPostingRepository.save(fullTime1);
        jobPostingRepository.save(fullTime2);
        jobPostingRepository.save(partTime);

        // When
        List<JobPosting> fullTimeJobs = jobPostingRepository.findByJobType(JobType.FULL_TIME);

        // Then
        assertThat(fullTimeJobs).hasSize(2);
        assertThat(fullTimeJobs).hasSize(2);
        assertThat(fullTimeJobs).allMatch(job -> job.getJobType() == JobType.FULL_TIME);
    }

    @Test
    @DisplayName("경력별 채용공고를 조회할 수 있다")
    void canFindJobPostingsByExperienceLevel() {
        // Given
        JobPosting junior = new JobPosting(companyUser, "주니어 개발자", "테스트기업", 
                                          "서울", JobType.FULL_TIME, ExperienceLevel.JUNIOR);
        junior.publish(LocalDate.now().plusDays(30));
        
        JobPosting mid = new JobPosting(companyUser, "중급 개발자", "테스트기업", 
                                       "부산", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        mid.publish(LocalDate.now().plusDays(20));
        
        JobPosting senior = new JobPosting(companyUser, "시니어 개발자", "테스트기업", 
                                          "대구", JobType.FULL_TIME, ExperienceLevel.SENIOR);
        senior.publish(LocalDate.now().plusDays(25));
        
        jobPostingRepository.save(junior);
        jobPostingRepository.save(mid);
        jobPostingRepository.save(senior);

        // When
        List<JobPosting> midLevelJobs = jobPostingRepository.findByExperienceLevel(ExperienceLevel.MID_LEVEL);

        // Then
        assertThat(midLevelJobs).hasSize(1);
        assertThat(midLevelJobs).allMatch(job -> job.getExperienceLevel() == ExperienceLevel.MID_LEVEL);
    }

    @Test
    @DisplayName("제목으로 채용공고를 검색할 수 있다")
    void canSearchJobPostingsByTitle() {
        // Given
        JobPosting backend = new JobPosting(companyUser, "백엔드 개발자", "테스트기업", 
                                           "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        backend.publish(LocalDate.now().plusDays(30));
        
        JobPosting frontend = new JobPosting(companyUser, "프론트엔드 개발자", "테스트기업", 
                                            "부산", JobType.FULL_TIME, ExperienceLevel.JUNIOR);
        frontend.publish(LocalDate.now().plusDays(20));
        
        JobPosting designer = new JobPosting(companyUser, "UI/UX 디자이너", "테스트기업", 
                                            "대구", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        designer.publish(LocalDate.now().plusDays(25));
        
        jobPostingRepository.save(backend);
        jobPostingRepository.save(frontend);
        jobPostingRepository.save(designer);

        // When
        List<JobPosting> developerJobs = jobPostingRepository.findByTitleContainingOrDescriptionContaining("개발자");

        // Then
        assertThat(developerJobs).hasSize(2);
        assertThat(developerJobs).hasSize(2);
    }

    @Test
    @DisplayName("기업 사용자별 채용공고를 조회할 수 있다")
    void canFindJobPostingsByCompanyUser() {
        // Given
        JobPosting job1 = new JobPosting(companyUser, "개발자1", "테스트기업", 
                                        "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        JobPosting job2 = new JobPosting(companyUser, "개발자2", "테스트기업", 
                                        "부산", JobType.FULL_TIME, ExperienceLevel.JUNIOR);
        
        jobPostingRepository.save(job1);
        jobPostingRepository.save(job2);

        // When
        List<JobPosting> companyJobs = jobPostingRepository.findByCompanyUser(companyUser);

        // Then
        assertThat(companyJobs).hasSize(2);
        assertThat(companyJobs).hasSize(2);
        assertThat(companyJobs).allMatch(job -> job.getCompanyUser().equals(companyUser));
    }

    @Test
    @DisplayName("페이징으로 채용공고를 조회할 수 있다")
    void canFindJobPostingsWithPaging() {
        // Given
        for (int i = 1; i <= 5; i++) {
            JobPosting job = new JobPosting(companyUser, "개발자" + i, "테스트기업", 
                                           "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
            job.publish(LocalDate.now().plusDays(30));
            jobPostingRepository.save(job);
        }

        // When
        Pageable pageable = PageRequest.of(0, 3);
        Page<JobPosting> jobPage = jobPostingRepository.findByStatus(JobStatus.PUBLISHED, pageable);
        List<JobPosting> jobs = jobPage.getContent();

        // Then
        assertThat(jobs).hasSize(3);
    }

    @Test
    @DisplayName("마감일이 지난 채용공고를 조회할 수 있다")
    void canFindExpiredJobPostings() {
        // Given
        JobPosting expired1 = new JobPosting(companyUser, "만료된공고1", "테스트기업", 
                                            "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        expired1.publish(LocalDate.now().minusDays(1)); // 어제 마감
        
        JobPosting expired2 = new JobPosting(companyUser, "만료된공고2", "테스트기업", 
                                            "부산", JobType.FULL_TIME, ExperienceLevel.JUNIOR);
        expired2.publish(LocalDate.now().minusDays(5)); // 5일전 마감
        
        JobPosting active = new JobPosting(companyUser, "활성공고", "테스트기업", 
                                          "대구", JobType.FULL_TIME, ExperienceLevel.SENIOR);
        active.publish(LocalDate.now().plusDays(10)); // 10일 후 마감
        
        jobPostingRepository.save(expired1);
        jobPostingRepository.save(expired2);
        jobPostingRepository.save(active);

        // When
        List<JobPosting> expiredJobs = jobPostingRepository.findByStatusAndDeadlineDateBefore(
                JobStatus.PUBLISHED, LocalDate.now());

        // Then
        assertThat(expiredJobs).hasSize(2);
        assertThat(expiredJobs).hasSize(2);
    }

    @Test
    @DisplayName("채용공고를 삭제할 수 있다")
    void canDeleteJobPosting() {
        // Given
        JobPosting jobPosting = new JobPosting(companyUser, "삭제할공고", "테스트기업", 
                                              "서울", JobType.FULL_TIME, ExperienceLevel.MID_LEVEL);
        JobPosting saved = jobPostingRepository.save(jobPosting);

        // When
        jobPostingRepository.delete(saved);

        // Then
        Optional<JobPosting> found = jobPostingRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}