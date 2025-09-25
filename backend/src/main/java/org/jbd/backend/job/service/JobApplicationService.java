    package org.jbd.backend.job.service;
    
    import lombok.RequiredArgsConstructor;
    import org.jbd.backend.job.domain.JobApplication;
    import org.jbd.backend.job.domain.JobPosting;
    import org.jbd.backend.job.domain.enums.ApplicationStatus;
    import org.jbd.backend.job.repository.JobApplicationRepository;
    import org.jbd.backend.job.repository.JobPostingRepository;
    import org.jbd.backend.user.domain.User;
    import org.jbd.backend.user.repository.UserRepository;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    
    import java.util.List;
    
    @Service
    @RequiredArgsConstructor
    @Transactional(readOnly = true)
    public class JobApplicationService {
    
        private final JobApplicationRepository jobApplicationRepository;
        private final JobPostingRepository jobPostingRepository;
        private final UserRepository userRepository;
    
        @Transactional
        public JobApplication applyToJobPosting(Long userId, Long jobPostingId, String coverLetter) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    
            JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                    .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
    
            if (jobApplicationRepository.existsByUserAndJobPosting(user, jobPosting)) {
                throw new IllegalStateException("이미 지원한 채용공고입니다.");
            }
    
            if (!jobPosting.canApply()) {
                throw new IllegalStateException("지원할 수 없는 채용공고입니다.");
            }
    
            JobApplication jobApplication = new JobApplication(user, jobPosting, coverLetter);
            return jobApplicationRepository.save(jobApplication);
        }
    
        public JobApplication getJobApplication(Long jobApplicationId) {
            return jobApplicationRepository.findByIdWithDetails(jobApplicationId)
                    .orElseThrow(() -> new IllegalArgumentException("지원서를 찾을 수 없습니다."));
        }
    
        public List<JobApplication> getJobApplicationsByUser(User user) {
            return jobApplicationRepository.findByUser(user);
        }
    
        public Page<JobApplication> getJobApplicationsByJobPosting(JobPosting jobPosting, Pageable pageable) {
            return jobApplicationRepository.findByJobPostingWithDetails(jobPosting, pageable);
        }
    
        @Transactional
        public JobApplication reviewJobApplication(Long jobApplicationId) {
            JobApplication jobApplication = getJobApplication(jobApplicationId);
            jobApplication.review();
            return jobApplicationRepository.save(jobApplication);
        }
    
        @Transactional
        public JobApplication passDocumentReview(Long jobApplicationId) {
            JobApplication jobApplication = getJobApplication(jobApplicationId);
            jobApplication.passDocumentReview();
            return jobApplicationRepository.save(jobApplication);
        }
    
        @Transactional
        public JobApplication passDocumentReview(Long jobApplicationId, Long userId) {
            JobApplication jobApplication = getJobApplication(jobApplicationId);
    
            // 권한 확인 - 채용공고 작성자인지 확인
            if (!jobApplication.getJobPosting().getCompanyUser().getId().equals(userId)) {
                throw new IllegalArgumentException("처리 권한이 없습니다");
            }
    
            jobApplication.passDocumentReview();
            return jobApplicationRepository.save(jobApplication);
        }
    
        @Transactional
        public JobApplication passInterview(Long jobApplicationId) {
            JobApplication jobApplication = getJobApplication(jobApplicationId);
            jobApplication.passInterview();
            return jobApplicationRepository.save(jobApplication);
        }
    
        @Transactional
        public JobApplication hireApplicant(Long jobApplicationId) {
            JobApplication jobApplication = getJobApplication(jobApplicationId);
            jobApplication.hire();
            return jobApplicationRepository.save(jobApplication);
        }
    
        @Transactional
        public JobApplication rejectJobApplication(Long jobApplicationId, String rejectionReason) {
            JobApplication jobApplication = getJobApplication(jobApplicationId);
            jobApplication.reject(rejectionReason);
            return jobApplicationRepository.save(jobApplication);
        }
    
        public List<JobApplication> getJobApplicationsByStatus(ApplicationStatus status) {
            return jobApplicationRepository.findByStatus(status);
        }
    
        public long getApplicationCountByJobPosting(JobPosting jobPosting) {
            return jobApplicationRepository.countByJobPosting(jobPosting);
        }
    
        public List<JobApplication> getJobApplicationsByUserAndStatus(User user, ApplicationStatus status) {
            return jobApplicationRepository.findByUserAndStatus(user, status);
        }
    
        public Page<JobApplication> getJobApplicationsByUser(User user, Pageable pageable) {
            return jobApplicationRepository.findByUserWithDetails(user, pageable);
        }
    
        public Page<JobApplication> getJobApplicationsByJobPostingAndStatus(JobPosting jobPosting, ApplicationStatus status, Pageable pageable) {
            return jobApplicationRepository.findByJobPostingAndStatus(jobPosting, status, pageable);
        }
    }