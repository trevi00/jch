package org.jbd.backend.user.domain.enums;

public enum EmploymentType {
    FULL_TIME("정규직"),
    PART_TIME("파트타임"),
    CONTRACT("계약직"),
    FREELANCE("프리랜서"),
    INTERNSHIP("인턴십"),
    VOLUNTEER("자원봉사"),
    PROJECT_BASED("프로젝트 기반"),
    REMOTE("원격근무"),
    HYBRID("하이브리드"),
    OTHER("기타");
    
    private final String description;
    
    EmploymentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}