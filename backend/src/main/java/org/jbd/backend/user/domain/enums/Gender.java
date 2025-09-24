package org.jbd.backend.user.domain.enums;

public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    OTHER("기타");
    
    private final String description;
    
    Gender(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}