package org.jbd.backend.user.domain.enums;

public enum CertificationStatus {
    ACTIVE("활성"),
    EXPIRED("만료"),
    REVOKED("취소"),
    PENDING("대기");

    private final String description;

    CertificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}