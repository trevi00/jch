package org.jbd.backend.user.domain;

import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.enums.SkillCategory;

@Entity
@Table(name = "skills")
public class SkillMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Long id;

    @Column(name = "skill_name", nullable = false, unique = true)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private SkillCategory category;

    public SkillMaster() {}

    public SkillMaster(String skillName, SkillCategory category) {
        this.skillName = skillName;
        this.category = category;
    }

    // Business methods
    public void updateSkillInfo(String skillName, SkillCategory category) {
        this.skillName = skillName;
        this.category = category;
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

    public SkillCategory getCategory() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }
}