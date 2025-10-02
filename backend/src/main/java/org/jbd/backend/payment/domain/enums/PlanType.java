package org.jbd.backend.payment.domain.enums;

/**
 * 구독 플랜 타입
 */
public enum PlanType {
    FREE_ACADEMY("솔데스크 학원 3개월 무료"),
    PAID_MONTHLY("월 정액제");

    private final String description;

    PlanType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}