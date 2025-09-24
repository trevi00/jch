package org.jbd.backend.user.dto;

import org.jbd.backend.user.domain.UserSkill;
import org.jbd.backend.user.domain.enums.SkillCategory;
import org.jbd.backend.user.domain.enums.SkillLevel;

// Wrapper class to maintain frontend compatibility
public class SkillDto {
    private Long id;
    private String skillName;
    private SkillCategory skillCategory;
    private SkillLevel skillLevel;
    private Integer yearsOfExperience;
    private String description;

    public static SkillDto from(UserSkill userSkill) {
        SkillDto dto = new SkillDto();
        dto.id = userSkill.getId();
        dto.skillName = userSkill.getSkill().getSkillName();
        dto.skillCategory = userSkill.getSkill().getCategory();
        dto.skillLevel = userSkill.getProficiencyLevel();
        dto.yearsOfExperience = userSkill.getYearsOfExperience();
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillCategory getSkillCategory() {
        return skillCategory;
    }

    public void setSkillCategory(SkillCategory skillCategory) {
        this.skillCategory = skillCategory;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}