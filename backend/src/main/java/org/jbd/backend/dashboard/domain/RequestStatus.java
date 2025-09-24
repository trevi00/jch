package org.jbd.backend.dashboard.domain;

public enum RequestStatus {
    PENDING("대기중"),
    APPROVED("승인됨"),
    REJECTED("거부됨"),
    COMPLETED("완료됨");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}