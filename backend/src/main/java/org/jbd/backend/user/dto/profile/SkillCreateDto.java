package org.jbd.backend.user.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jbd.backend.user.domain.enums.SkillCategory;
import org.jbd.backend.user.domain.enums.SkillLevel;

public class SkillCreateDto {
    
    @NotBlank(message = "스킬명은 필수입니다.")
    private String skillName;
    
    @NotNull(message = "스킬 카테고리는 필수입니다.")
    private SkillCategory skillCategory;
    
    @NotNull(message = "스킬 레벨은 필수입니다.")
    private SkillLevel skillLevel;
    
    private Integer yearsOfExperience;
    private String description;
    
    protected SkillCreateDto() {}
    
    public SkillCreateDto(String skillName, SkillCategory skillCategory, SkillLevel skillLevel,
                         Integer yearsOfExperience, String description) {
        this.skillName = skillName;
        this.skillCategory = skillCategory;
        this.skillLevel = skillLevel;
        this.yearsOfExperience = yearsOfExperience;
        this.description = description;
    }
    
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

    // Setters for JSON deserialization
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public void setSkillCategory(SkillCategory skillCategory) {
        this.skillCategory = skillCategory;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }


    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}