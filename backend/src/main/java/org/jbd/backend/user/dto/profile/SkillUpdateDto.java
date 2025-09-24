package org.jbd.backend.user.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jbd.backend.user.domain.enums.SkillCategory;
import org.jbd.backend.user.domain.enums.SkillLevel;

public class SkillUpdateDto {
    
    @NotBlank(message = "스킬명은 필수입니다.")
    private String skillName;
    
    @NotNull(message = "스킬 카테고리는 필수입니다.")
    private SkillCategory skillCategory;
    
    @NotNull(message = "스킬 레벨은 필수입니다.")
    private SkillLevel skillLevel;
    
    private Integer yearsOfExperience;
    private String description;
    
    protected SkillUpdateDto() {}
    
    // Getters
    public String getSkillName() {
        return skillName;
    }
    
    public SkillCategory getSkillCategory() {
        return skillCategory;
    }
    
    public SkillLevel getSkillLevel() {
        return skillLevel;
    }
    
    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }
    
    public String getDescription() {
        return description;
    }
}