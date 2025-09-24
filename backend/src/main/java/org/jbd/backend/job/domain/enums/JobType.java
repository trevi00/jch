package org.jbd.backend.job.domain.enums;

public enum JobType {
    FULL_TIME("정규직"),
    PART_TIME("파트타임"),
    CONTRACT("계약직"),
    INTERNSHIP("인턴십"),
    FREELANCE("프리랜서"),
    PROJECT_BASED("프로젝트 기반"),
    TEMPORARY("임시직"),
    REMOTE("원격근무"),
    HYBRID("하이브리드");
    
    private final String description;
    
    JobType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}