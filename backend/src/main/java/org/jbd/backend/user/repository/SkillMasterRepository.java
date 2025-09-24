package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.SkillMaster;
import org.jbd.backend.user.domain.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillMasterRepository extends JpaRepository<SkillMaster, Long> {

    Optional<SkillMaster> findBySkillName(String skillName);

    List<SkillMaster> findByCategory(SkillCategory category);

    boolean existsBySkillName(String skillName);
}