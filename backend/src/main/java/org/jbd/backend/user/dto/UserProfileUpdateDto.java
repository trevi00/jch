package org.jbd.backend.user.dto;

import jakarta.validation.constraints.Size;
import org.jbd.backend.user.domain.enums.Gender;

public class UserProfileUpdateDto {

    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String firstName;

    @Size(max = 50, message = "성은 50자 이하여야 합니다.")
    private String lastName;

    private Integer age;

    private Gender gender;

    @Size(max = 100, message = "위치는 100자 이하여야 합니다.")
    private String location;

    @Size(max = 100, message = "희망 직무는 100자 이하여야 합니다.")
    private String desiredJob;

    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String phoneNumber;

    @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.")
    private String profileImageUrl;

    @Size(max = 1000, message = "자기소개는 1000자 이하여야 합니다.")
    private String bio;

    public UserProfileUpdateDto() {}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDesiredJob() {
        return desiredJob;
    }

    public void setDesiredJob(String desiredJob) {
        this.desiredJob = desiredJob;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}