package org.jbd.backend.user.domain.enums;

public enum UserType {
    GENERAL("일반 유저"),
    COMPANY("기업 유저"),
    ADMIN("관리자");
    
    private final String description;
    
    UserType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}