package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    
    List<Certification> findByUserIdOrderByIssueDateDesc(Long userId);
    
    List<Certification> findByUserIdAndIsActiveTrue(Long userId);
    
    List<Certification> findByUserIdAndExpiryDateBefore(Long userId, LocalDate date);
    
    List<Certification> findByUserIdAndIssuingOrganization(Long userId, String issuingOrganization);
    
    long countByUserIdAndIsActiveTrue(Long userId);
    
    long countByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}