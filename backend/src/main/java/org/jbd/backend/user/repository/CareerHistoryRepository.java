package org.jbd.backend.user.repository;

import org.jbd.backend.user.domain.CareerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareerHistoryRepository extends JpaRepository<CareerHistory, Long> {

    List<CareerHistory> findByUserId(Long userId);

    List<CareerHistory> findByUserIdAndIsCurrent(Long userId, Boolean isCurrent);

    List<CareerHistory> findByUserIdOrderByStartDateDesc(Long userId);

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);
}