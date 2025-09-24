package org.jbd.backend.user.domain.enums;

public enum EducationType {
    HIGH_SCHOOL("고등학교"),
    UNIVERSITY("대학교"),
    GRADUATE_SCHOOL("대학원"),
    VOCATIONAL_SCHOOL("전문학교"),
    CERTIFICATE_COURSE("자격증 과정"),
    ONLINE_COURSE("온라인 과정"),
    BOOTCAMP("부트캠프"),
    OTHER("기타");

    private final String description;

    EducationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}