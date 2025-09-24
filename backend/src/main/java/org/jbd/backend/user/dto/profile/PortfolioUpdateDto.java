package org.jbd.backend.user.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jbd.backend.user.domain.enums.PortfolioType;
import java.time.LocalDate;

public class PortfolioUpdateDto {
    
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    private String description;
    
    @NotNull(message = "포트폴리오 타입은 필수입니다.")
    private PortfolioType portfolioType;
    
    private String projectUrl;
    private String githubUrl;
    private String demoUrl;
    private String imageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String technologiesUsed;
    private Integer teamSize;
    private String myRole;
    
    protected PortfolioUpdateDto() {}
    
    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public PortfolioType getPortfolioType() { return portfolioType; }
    public String getProjectUrl() { return projectUrl; }
    public String getGithubUrl() { return githubUrl; }
    public String getDemoUrl() { return demoUrl; }
    public String getImageUrl() { return imageUrl; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getTechnologiesUsed() { return technologiesUsed; }
    public Integer getTeamSize() { return teamSize; }
    public String getMyRole() { return myRole; }
}