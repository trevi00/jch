package org.jbd.backend.user.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SkillLevel {
    BEGINNER("초급"),
    INTERMEDIATE("중급"),
    ADVANCED("고급"),
    EXPERT("전문가");

    private final String description;

    SkillLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonValue
    public String getJsonValue() {
        return this.name();
    }

    @JsonCreator
    public static SkillLevel fromValue(Object value) {
        if (value instanceof String) {
            try {
                return SkillLevel.valueOf((String) value);
            } catch (IllegalArgumentException e) {
                return BEGINNER; // default fallback
            }
        } else if (value instanceof Integer) {
            int level = (Integer) value;
            switch (level) {
                case 1:
                    return BEGINNER;
                case 2:
                    return INTERMEDIATE;
                case 3:
                    return INTERMEDIATE;
                case 4:
                    return ADVANCED;
                case 5:
                    return EXPERT;
                default:
                    return BEGINNER;
            }
        }
        return BEGINNER; // default fallback
    }
}