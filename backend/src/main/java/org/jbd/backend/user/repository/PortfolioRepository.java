package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.Portfolio;
import org.jbd.backend.user.domain.enums.PortfolioType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    List<Portfolio> findByUserIdOrderByDisplayOrderAsc(Long userId);
    
    List<Portfolio> findByUserIdAndIsFeaturedTrueOrderByDisplayOrderAsc(Long userId);
    
    List<Portfolio> findByUserIdAndPortfolioType(Long userId, PortfolioType portfolioType);
    
    long countByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}