package org.jbd.backend.ai.domain;

public enum InterviewStatus {
    IN_PROGRESS("진행중"),
    COMPLETED("완료"),
    CANCELLED("취소");
    
    private final String description;
    
    InterviewStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}