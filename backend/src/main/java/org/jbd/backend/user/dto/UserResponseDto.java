package org.jbd.backend.user.dto;

import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.UserProfile;
import org.jbd.backend.user.domain.enums.EmploymentStatus;
import org.jbd.backend.user.domain.enums.Gender;
import org.jbd.backend.user.domain.enums.UserType;

import java.time.LocalDateTime;

public class UserResponseDto {
    
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String phoneNumber;
    private Integer age;
    private Gender gender;
    private String residenceRegion;
    private String desiredJob;
    private String desiredCompany;
    private EmploymentStatus employmentStatus;
    private UserType userType;
    private Boolean emailVerified;
    private String companyEmail;
    private Boolean companyEmailVerified;
    private String oauthProvider;
    private String profileImageUrl;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime adminConvertedAt;
    private Boolean isActive;
    
    public UserResponseDto() {}
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static UserResponseDto from(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.id = user.getId();
        dto.email = user.getEmail();
        dto.userType = user.getUserType();
        dto.oauthProvider = user.getOauthProvider() != null ? user.getOauthProvider().name() : null;
        dto.employmentStatus = user.getEmploymentStatus();
        dto.emailVerified = user.getEmailVerified();
        dto.companyEmailVerified = user.getCompanyEmailVerified();
        dto.createdAt = user.getCreatedAt();
        return dto;
    }

    public static UserResponseDto from(User user, UserProfile profile) {
        UserResponseDto dto = from(user);
        if (profile != null) {
            dto.name = profile.getFullName();
            dto.nickname = profile.getFirstName(); // 임시로 firstName을 nickname으로 사용
            dto.phoneNumber = profile.getPhoneNumber();
            dto.age = profile.getAge();
            dto.gender = profile.getGender();
            dto.residenceRegion = profile.getLocation();
            dto.desiredJob = profile.getDesiredJob();
            dto.profileImageUrl = profile.getProfileImageUrl();
        }
        return dto;
    }
    
    public static UserResponseDto fromPublic(User user, UserProfile profile) {
        UserResponseDto dto = new UserResponseDto();
        dto.id = user.getId();
        dto.userType = user.getUserType();
        dto.createdAt = user.getCreatedAt();
        if (profile != null) {
            dto.name = profile.getFullName();
            dto.nickname = profile.getFirstName();
            dto.profileImageUrl = profile.getProfileImageUrl();
        }
        return dto;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Integer getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public String getResidenceRegion() {
        return residenceRegion;
    }

    public String getDesiredJob() {
        return desiredJob;
    }

    public String getDesiredCompany() {
        return desiredCompany;
    }
    
    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public String getCompanyEmail() {
        return companyEmail;
    }

    public Boolean getCompanyEmailVerified() {
        return companyEmailVerified;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAdminConvertedAt() {
        return adminConvertedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Boolean isActive() {
        return isActive;
    }

    public Boolean isEmailVerified() {
        return emailVerified;
    }

    public Boolean isCompanyEmailVerified() {
        return companyEmailVerified;
    }
    
    public static class Builder {
        private UserResponseDto dto = new UserResponseDto();

        public Builder id(Long id) {
            dto.id = id;
            return this;
        }

        public Builder email(String email) {
            dto.email = email;
            return this;
        }

        public Builder name(String name) {
            dto.name = name;
            return this;
        }

        public Builder userType(UserType userType) {
            dto.userType = userType;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            dto.isActive = isActive;
            return this;
        }

        public Builder emailVerified(Boolean emailVerified) {
            dto.emailVerified = emailVerified;
            return this;
        }

        public Builder companyEmailVerified(Boolean companyEmailVerified) {
            dto.companyEmailVerified = companyEmailVerified;
            return this;
        }

        public Builder lastLoginAt(LocalDateTime lastLoginAt) {
            dto.lastLoginAt = lastLoginAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            dto.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            dto.updatedAt = updatedAt;
            return this;
        }

        public UserResponseDto build() {
            return dto;
        }
    }
}