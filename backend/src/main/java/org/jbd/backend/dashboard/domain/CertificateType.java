package org.jbd.backend.dashboard.domain;

public enum CertificateType {
    COMPLETION_CERTIFICATE("수료증"),
    ENROLLMENT_CERTIFICATE("재학증명서"),
    COURSE_COMPLETION_CERTIFICATE("이수증명서"),
    TRANSCRIPT("성적증명서"),
    ATTENDANCE_CERTIFICATE("출석증명서");

    private final String description;

    CertificateType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}