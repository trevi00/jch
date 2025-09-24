package org.jbd.backend.user.domain.enums;

public enum EmploymentStatus {
    JOB_SEEKING("구직중"),
    EMPLOYED("재직중"),
    PREPARING("취업준비중"),
    STUDENT("학생");
    
    private final String description;
    
    EmploymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}