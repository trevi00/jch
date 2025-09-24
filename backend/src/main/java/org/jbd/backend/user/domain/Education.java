package org.jbd.backend.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.enums.EducationLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_education")
public class Education extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", nullable = false)
    private EducationLevel educationLevel;
    
    @Column(name = "school_name", nullable = false)
    private String schoolName;
    
    @Column(name = "major")
    private String major;
    
    @Column(name = "graduation_year")
    private Integer graduationYear;
    
    @Column(name = "graduation_date")
    private LocalDate graduationDate;
    
    @Column(name = "gpa", precision = 3, scale = 2)
    private BigDecimal gpa;
    
    @Column(name = "max_gpa", precision = 3, scale = 2)
    private BigDecimal maxGpa;
    
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;
    
    protected Education() {}
    
    public Education(User user, EducationLevel educationLevel, String schoolName, String major, Integer graduationYear) {
        this.user = user;
        this.educationLevel = educationLevel;
        this.schoolName = schoolName;
        this.major = major;
        this.graduationYear = graduationYear;
    }
    
    public void updateEducationInfo(EducationLevel educationLevel, String schoolName, String major, 
                                   Integer graduationYear, LocalDate graduationDate, BigDecimal gpa, BigDecimal maxGpa) {
        this.educationLevel = educationLevel;
        this.schoolName = schoolName;
        this.major = major;
        this.graduationYear = graduationYear;
        this.graduationDate = graduationDate;
        this.gpa = gpa;
        this.maxGpa = maxGpa;
    }
    
    public void markAsCurrent() {
        this.isCurrent = true;
    }
    
    public void markAsCompleted() {
        this.isCurrent = false;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public EducationLevel getEducationLevel() {
        return educationLevel;
    }
    
    public String getSchoolName() {
        return schoolName;
    }
    
    public String getMajor() {
        return major;
    }
    
    public Integer getGraduationYear() {
        return graduationYear;
    }
    
    public LocalDate getGraduationDate() {
        return graduationDate;
    }
    
    public BigDecimal getGpa() {
        return gpa;
    }
    
    public BigDecimal getMaxGpa() {
        return maxGpa;
    }
    
    public Boolean getIsCurrent() {
        return isCurrent;
    }
}