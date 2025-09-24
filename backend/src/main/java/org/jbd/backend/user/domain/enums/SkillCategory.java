package org.jbd.backend.user.domain.enums;

public enum SkillCategory {
    PROGRAMMING_LANGUAGE("프로그래밍 언어"),
    FRAMEWORK("프레임워크"),
    DATABASE("데이터베이스"),
    CLOUD("클라우드"),
    DEVOPS("DevOps"),
    FRONTEND("프론트엔드"),
    BACKEND("백엔드"),
    MOBILE("모바일"),
    DATA_SCIENCE("데이터 사이언스"),
    AI_ML("AI/ML"),
    DESIGN("디자인"),
    PROJECT_MANAGEMENT("프로젝트 관리"),
    COMMUNICATION("커뮤니케이션"),
    LANGUAGE("외국어"),
    CERTIFICATION("자격증"),
    OTHER("기타");
    
    private final String description;
    
    SkillCategory(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}