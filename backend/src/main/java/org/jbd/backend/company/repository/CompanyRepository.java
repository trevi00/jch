package org.jbd.backend.company.repository;

import org.jbd.backend.company.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByUserId(Long userId);

    Optional<Company> findByBusinessNumber(String businessNumber);

    Optional<Company> findByCompanyName(String companyName);

    @Query("SELECT c FROM Company c WHERE c.user.id = :userId")
    Optional<Company> findByUserIdWithUser(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByBusinessNumber(String businessNumber);

    boolean existsByCompanyName(String companyName);
}