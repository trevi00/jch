package org.jbd.backend.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.enums.SkillLevel;

@Entity
@Table(name = "user_skills")
public class UserSkill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_skill_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private SkillMaster skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level", nullable = false)
    private SkillLevel proficiencyLevel = SkillLevel.BEGINNER;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience = 0;

    protected UserSkill() {}

    public UserSkill(User user, SkillMaster skill, SkillLevel proficiencyLevel) {
        this.user = user;
        this.skill = skill;
        this.proficiencyLevel = proficiencyLevel;
    }

    // Business methods
    public void updateProficiency(SkillLevel proficiencyLevel, Integer yearsOfExperience) {
        this.proficiencyLevel = proficiencyLevel;
        this.yearsOfExperience = yearsOfExperience;
    }

    public void updateExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
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

    public SkillMaster getSkill() {
        return skill;
    }

    public void setSkill(SkillMaster skill) {
        this.skill = skill;
    }

    public SkillLevel getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(SkillLevel proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
}