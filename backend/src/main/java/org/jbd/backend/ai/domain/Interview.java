package org.jbd.backend.ai.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviews")
public class Interview extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long id;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "job_role", nullable = false)
    private String jobRole;
    
    @Column(name = "interview_type", nullable = false)
    private String interviewType; // "technical" or "personality"
    
    @Column(name = "experience_level")
    private String experienceLevel; // "entry", "junior", "senior"
    
    @Column(name = "overall_score")
    private BigDecimal overallScore;
    
    @Column(name = "total_questions")
    private Integer totalQuestions;
    
    @Column(name = "answered_questions")
    private Integer answeredQuestions;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private InterviewStatus status = InterviewStatus.IN_PROGRESS;
    
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewQuestion> questions = new ArrayList<>();
    
    public Interview() {}
    
    public Interview(User user, String jobRole, String interviewType, String experienceLevel) {
        this.user = user;
        this.jobRole = jobRole;
        this.interviewType = interviewType;
        this.experienceLevel = experienceLevel;
        this.status = InterviewStatus.IN_PROGRESS;
    }
    
    public void complete(BigDecimal overallScore, Integer durationMinutes) {
        this.status = InterviewStatus.COMPLETED;
        this.overallScore = overallScore;
        this.completedAt = LocalDateTime.now();
        this.durationMinutes = durationMinutes;
        this.answeredQuestions = (int) questions.stream().filter(q -> q.getAnswer() != null).count();
        this.totalQuestions = questions.size();
    }
    
    public void addQuestion(InterviewQuestion question) {
        questions.add(question);
        question.setInterview(this);
    }
    
    public void removeQuestion(InterviewQuestion question) {
        questions.remove(question);
        question.setInterview(null);
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getJobRole() {
        return jobRole;
    }
    
    public String getInterviewType() {
        return interviewType;
    }
    
    public String getExperienceLevel() {
        return experienceLevel;
    }
    
    public BigDecimal getOverallScore() {
        return overallScore;
    }
    
    public Integer getTotalQuestions() {
        return totalQuestions;
    }
    
    public Integer getAnsweredQuestions() {
        return answeredQuestions;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public InterviewStatus getStatus() {
        return status;
    }
    
    public List<InterviewQuestion> getQuestions() {
        return questions;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }
    
    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }
    
    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }
    
    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }
    
    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public void setAnsweredQuestions(Integer answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public void setStatus(InterviewStatus status) {
        this.status = status;
    }
    
    public void setQuestions(List<InterviewQuestion> questions) {
        this.questions = questions;
    }
}