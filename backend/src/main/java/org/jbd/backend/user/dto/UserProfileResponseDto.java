package org.jbd.backend.user.dto;

import org.jbd.backend.user.domain.UserProfile;
import org.jbd.backend.user.domain.enums.Gender;

import java.time.LocalDateTime;

public class UserProfileResponseDto {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private Integer age;
    private Gender gender;
    private String location;
    private String desiredJob;
    private String phoneNumber;
    private String profileImageUrl;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserProfileResponseDto() {}

    public static UserProfileResponseDto from(UserProfile userProfile) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.id = userProfile.getId();
        dto.userId = userProfile.getUser().getId();
        dto.firstName = userProfile.getFirstName();
        dto.lastName = userProfile.getLastName();
        dto.fullName = userProfile.getFullName();
        dto.age = userProfile.getAge();
        dto.gender = userProfile.getGender();
        dto.location = userProfile.getLocation();
        dto.desiredJob = userProfile.getDesiredJob();
        dto.phoneNumber = userProfile.getPhoneNumber();
        dto.profileImageUrl = userProfile.getProfileImageUrl();
        dto.bio = userProfile.getBio();
        dto.createdAt = userProfile.getCreatedAt();
        dto.updatedAt = userProfile.getUpdatedAt();
        return dto;
    }

    public static UserProfileResponseDto fromPublic(UserProfile userProfile) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.id = userProfile.getId();
        dto.userId = userProfile.getUser().getId();
        dto.firstName = userProfile.getFirstName();
        dto.lastName = userProfile.getLastName();
        dto.fullName = userProfile.getFullName();
        dto.age = userProfile.getAge();
        dto.gender = userProfile.getGender();
        dto.location = userProfile.getLocation();
        dto.desiredJob = userProfile.getDesiredJob();
        dto.profileImageUrl = userProfile.getProfileImageUrl();
        dto.bio = userProfile.getBio();
        return dto;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public Integer getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public String getLocation() {
        return location;
    }

    public String getDesiredJob() {
        return desiredJob;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getBio() {
        return bio;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}