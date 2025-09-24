package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.Education;
import org.jbd.backend.user.domain.enums.EducationLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    
    List<Education> findByUserIdOrderByGraduationYearDesc(Long userId);
    
    Optional<Education> findByUserIdAndIsCurrentTrue(Long userId);
    
    List<Education> findByUserIdAndEducationLevel(Long userId, EducationLevel educationLevel);
    
    Optional<Education> findTopByUserIdOrderByEducationLevelDesc(Long userId);
    
    long countByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}