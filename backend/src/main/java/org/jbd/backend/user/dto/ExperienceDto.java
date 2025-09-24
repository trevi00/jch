package org.jbd.backend.user.dto;

import org.jbd.backend.user.domain.CareerHistory;
import org.jbd.backend.user.domain.enums.EmploymentType;

import java.time.LocalDate;

// Wrapper class to maintain frontend compatibility
public class ExperienceDto {
    private Long id;
    private String companyName;
    private String position;
    private String department;
    private EmploymentType employmentType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String description;
    private String responsibilities;
    private String achievements;
    private String technologiesUsed;
    private String location;
    private String salaryRange;

    public static ExperienceDto from(CareerHistory careerHistory) {
        ExperienceDto dto = new ExperienceDto();
        dto.id = careerHistory.getId();
        dto.companyName = careerHistory.getCompanyName();
        dto.position = careerHistory.getPosition();
        dto.department = careerHistory.getDepartment();
        dto.employmentType = careerHistory.getEmploymentType() != null ? careerHistory.getEmploymentType() : EmploymentType.FULL_TIME;
        dto.startDate = careerHistory.getStartDate();
        dto.endDate = careerHistory.getEndDate();
        dto.isCurrent = careerHistory.getIsCurrent();
        dto.description = careerHistory.getDescription();
        dto.achievements = careerHistory.getAchievements();
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
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

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getTechnologiesUsed() {
        return technologiesUsed;
    }

    public void setTechnologiesUsed(String technologiesUsed) {
        this.technologiesUsed = technologiesUsed;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }
}