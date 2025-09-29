package org.jbd.backend.payment.domain.enums;

/**
 * 구독 상태
 */
public enum SubscriptionStatus {
    ACTIVE("활성"),
    EXPIRED("만료"),
    CANCELLED("취소");

    private final String description;

    SubscriptionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}