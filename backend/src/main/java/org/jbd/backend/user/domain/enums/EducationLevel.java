package org.jbd.backend.user.domain.enums;

public enum EducationLevel {
    HIGH_SCHOOL("고등학교"),
    ASSOCIATE("전문대학"),
    BACHELOR("학사"),
    MASTER("석사"),
    DOCTORATE("박사"),
    BOOTCAMP("부트캠프"),
    VOCATIONAL("직업훈련원"),
    OTHER("기타");
    
    private final String description;
    
    EducationLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}