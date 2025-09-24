package org.jbd.backend.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.enums.EmploymentType;

import java.time.LocalDate;

@Entity
@Table(name = "career_histories")
public class CareerHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "career_id")
    private Long id;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "company_name", nullable = false)
    private String companyName;
    
    @Column(name = "position", nullable = false)
    private String position;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    protected CareerHistory() {}

    public CareerHistory(User user, String companyName, String position, LocalDate startDate) {
        this.user = user;
        this.companyName = companyName;
        this.position = position;
        this.startDate = startDate;
    }

    // Business methods
    public void updateCareerInfo(String companyName, String position, String department,
                                LocalDate startDate, LocalDate endDate, String description, String achievements) {
        this.companyName = companyName;
        this.position = position;
        this.department = department;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.achievements = achievements;
    }
    
    public void markAsCurrent() {
        this.isCurrent = true;
        this.endDate = null;
    }
    
    public void markAsCompleted(LocalDate endDate) {
        this.isCurrent = false;
        this.endDate = endDate;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public String getPosition() {
        return position;
    }
    
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
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

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }
}