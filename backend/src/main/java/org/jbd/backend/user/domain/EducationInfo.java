package org.jbd.backend.user.domain;

import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.enums.EducationType;

import java.time.LocalDate;

@Entity
@Table(name = "education_infos")
public class EducationInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "education_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_type", nullable = false)
    private EducationType educationType;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name = "major")
    private String major;

    @Column(name = "degree")
    private String degree;

    @Column(name = "grade_type")
    private String gradeType;

    @Column(name = "grade_value")
    private String gradeValue;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current_education")
    private Boolean isCurrentEducation = false;

    @Column(name = "location")
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;

    public EducationInfo() {}

    public EducationInfo(User user, EducationType educationType, String institutionName) {
        this.user = user;
        this.educationType = educationType;
        this.institutionName = institutionName;
    }

    // Business methods
    public void updateEducationInfo(String institutionName, String major, String degree) {
        this.institutionName = institutionName;
        this.major = major;
        this.degree = degree;
    }

    public void updateGrade(String gradeType, String gradeValue) {
        this.gradeType = gradeType;
        this.gradeValue = gradeValue;
    }

    public void updatePeriod(LocalDate startDate, LocalDate endDate, Boolean isCurrentEducation) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrentEducation = isCurrentEducation;
    }

    public boolean isCurrentEducation() {
        return Boolean.TRUE.equals(isCurrentEducation);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EducationType getEducationType() {
        return educationType;
    }

    public void setEducationType(EducationType educationType) {
        this.educationType = educationType;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getGradeType() {
        return gradeType;
    }

    public void setGradeType(String gradeType) {
        this.gradeType = gradeType;
    }

    public String getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(String gradeValue) {
        this.gradeValue = gradeValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsCurrentEducation() {
        return isCurrentEducation;
    }

    public void setIsCurrentEducation(Boolean isCurrentEducation) {
        this.isCurrentEducation = isCurrentEducation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }
}