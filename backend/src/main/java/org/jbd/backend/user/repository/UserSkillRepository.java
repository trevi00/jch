package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.UserSkill;
import org.jbd.backend.user.domain.enums.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    @Query("SELECT us FROM UserSkill us LEFT JOIN FETCH us.skill s WHERE us.user.id = :userId")
    List<UserSkill> findByUserId(@Param("userId") Long userId);

    @Query("SELECT us FROM UserSkill us JOIN us.skill s WHERE us.user.id = :userId AND s.category = :category")
    List<UserSkill> findByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    List<UserSkill> findByUserIdAndProficiencyLevel(Long userId, SkillLevel proficiencyLevel);

    @Query("SELECT us FROM UserSkill us JOIN us.skill s WHERE us.user.id = :userId AND s.skillName = :skillName")
    Optional<UserSkill> findByUserIdAndSkillName(@Param("userId") Long userId, @Param("skillName") String skillName);

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);
}