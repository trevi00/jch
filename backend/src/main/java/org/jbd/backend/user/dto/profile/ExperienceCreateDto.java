package org.jbd.backend.user.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jbd.backend.user.domain.enums.EmploymentType;
import java.time.LocalDate;

public class ExperienceCreateDto {
    
    @NotBlank(message = "회사명은 필수입니다.")
    private String companyName;
    
    @NotBlank(message = "직책은 필수입니다.")
    private String position;
    
    private String department;

    private EmploymentType employmentType;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isCurrentJob;
    private String description;
    private String responsibilities;
    private String achievements;
    private String technologiesUsed;
    private String location;
    private String salaryRange;
    
    protected ExperienceCreateDto() {}
    
    // Getters
    public String getCompanyName() { return companyName; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Boolean getIsCurrentJob() { return isCurrentJob; }
    public String getDescription() { return description; }
    public String getResponsibilities() { return responsibilities; }
    public String getAchievements() { return achievements; }
    public String getTechnologiesUsed() { return technologiesUsed; }
    public String getLocation() { return location; }
    public String getSalaryRange() { return salaryRange; }
}