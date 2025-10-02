package org.jbd.backend.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.payment.domain.enums.PlanType;
import org.jbd.backend.payment.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 구독 도메인 엔티티
 *
 * 사용자의 구독 정보를 관리하는 핵심 도메인 객체입니다.
 * 솔데스크 학원 무료 구독과 일반 사용자 월 정액제를 구분하여 관리합니다.
 *
 * 도메인 관계:
 * - User (N:1): 사용자별 구독 정보
 *
 * 비즈니스 규칙:
 * - FREE_ACADEMY: 솔데스크 학원 소속자 3개월 무료
 * - PAID_MONTHLY: 일반 사용자 월 1원 정액제
 * - 중복 활성 구독 불가
 * - 만료일 기준 자동 상태 관리
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "KAKAOPAY";

    @Column(name = "kakao_tid", length = 255)
    private String kakaoTid;

    @Column(name = "academy_name", length = 255)
    private String academyName;

    @Column(name = "academy_email", length = 255)
    private String academyEmail;

    @Column(name = "academy_verified", nullable = false)
    private Boolean academyVerified = false;

    @Column(name = "order_id", length = 255)
    private String orderId;

    public Subscription() {}

    public Subscription(User user, PlanType planType, LocalDateTime startDate, LocalDateTime endDate) {
        this.user = user;
        this.planType = planType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = SubscriptionStatus.ACTIVE;
    }

    // 비즈니스 메서드
    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE &&
               LocalDateTime.now().isBefore(endDate);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate) ||
               status == SubscriptionStatus.EXPIRED;
    }

    public void setKakaoPayment(String tid, String orderId, BigDecimal amount) {
        this.kakaoTid = tid;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = "KAKAOPAY";
    }

    public void setAcademyInfo(String academyName, String academyEmail) {
        this.academyName = academyName;
        this.academyEmail = academyEmail;
        this.academyVerified = true;
        this.amount = BigDecimal.ZERO;
    }

    public boolean isFreeAcademyPlan() {
        return planType == PlanType.FREE_ACADEMY;
    }

    public boolean isPaidMonthlyPlan() {
        return planType == PlanType.PAID_MONTHLY;
    }
}