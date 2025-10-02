package org.jbd.backend.payment.repository;

import org.jbd.backend.payment.domain.Subscription;
import org.jbd.backend.payment.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long userId, SubscriptionStatus status);

    Optional<Subscription> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    List<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.endDate < :now")
    List<Subscription> findExpiredSubscriptions(@Param("status") SubscriptionStatus status, @Param("now") LocalDateTime now);

    boolean existsByUserIdAndStatus(Long userId, SubscriptionStatus status);

    Optional<Subscription> findByOrderId(String orderId);

    Optional<Subscription> findByKakaoTid(String kakaoTid);
}