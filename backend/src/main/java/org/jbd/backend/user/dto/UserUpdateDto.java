package org.jbd.backend.user.dto;

import jakarta.validation.constraints.Size;
import org.jbd.backend.user.domain.enums.EmploymentStatus;
import org.jbd.backend.user.domain.enums.Gender;

public class UserUpdateDto {
    
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
    private String name;
    
    @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하여야 합니다.")
    private String nickname;
    
    @Size(min = 10, max = 15, message = "전화번호는 10자 이상 15자 이하여야 합니다.")
    private String phoneNumber;
    
    private Integer age;
    private Gender gender;
    
    @Size(max = 100, message = "거주지역은 100자 이하여야 합니다.")
    private String residenceRegion;
    
    @Size(max = 100, message = "희망직무는 100자 이하여야 합니다.")
    private String desiredJob;
    
    @Size(max = 100, message = "희망회사는 100자 이하여야 합니다.")
    private String desiredCompany;
    
    private EmploymentStatus employmentStatus;
    
    public UserUpdateDto() {}
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
    
    public String getResidenceRegion() {
        return residenceRegion;
    }
    
    public void setResidenceRegion(String residenceRegion) {
        this.residenceRegion = residenceRegion;
    }
    
    public String getDesiredJob() {
        return desiredJob;
    }
    
    public void setDesiredJob(String desiredJob) {
        this.desiredJob = desiredJob;
    }
    
    public String getDesiredCompany() {
        return desiredCompany;
    }
    
    public void setDesiredCompany(String desiredCompany) {
        this.desiredCompany = desiredCompany;
    }
    
    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }
    
    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }
}