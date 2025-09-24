package org.jbd.backend.user.domain.enums;

public enum PortfolioType {
    WEB_APPLICATION("웹 애플리케이션"),
    MOBILE_APPLICATION("모바일 애플리케이션"),
    DESKTOP_APPLICATION("데스크톱 애플리케이션"),
    API_BACKEND("API/백엔드"),
    DATA_ANALYSIS("데이터 분석"),
    MACHINE_LEARNING("머신러닝"),
    DESIGN("디자인"),
    RESEARCH("연구"),
    OPEN_SOURCE("오픈소스"),
    PERSONAL_PROJECT("개인 프로젝트"),
    TEAM_PROJECT("팀 프로젝트"),
    HACKATHON("해커톤"),
    COMPETITION("경진대회"),
    OTHER("기타");
    
    private final String description;
    
    PortfolioType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}