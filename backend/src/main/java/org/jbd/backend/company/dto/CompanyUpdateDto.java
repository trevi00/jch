package org.jbd.backend.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CompanyUpdateDto {

    @NotBlank(message = "회사명은 필수입니다")
    @Size(max = 100, message = "회사명은 100자 이하여야 합니다")
    private String companyName;

    @Size(max = 12, message = "사업자번호는 12자 이하여야 합니다")
    private String businessNumber;

    @Size(max = 50, message = "업종은 50자 이하여야 합니다")
    private String industry;

    @Size(max = 100, message = "위치는 100자 이하여야 합니다")
    private String location;

    private LocalDate establishmentDate;

    private Integer employeeCount;

    private BigDecimal revenue;

    private String description;

    @Size(max = 200, message = "웹사이트 URL은 200자 이하여야 합니다")
    private String websiteUrl;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "회사 이메일은 100자 이하여야 합니다")
    private String companyEmail;

    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    private String phoneNumber;

    @Size(max = 200, message = "주소는 200자 이하여야 합니다")
    private String address;

    @Size(max = 500, message = "로고 URL은 500자 이하여야 합니다")
    private String logoUrl;

    public CompanyUpdateDto() {}

    // Getters
    public String getCompanyName() {
        return companyName;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public String getIndustry() {
        return industry;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getEstablishmentDate() {
        return establishmentDate;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    // Setters
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setEstablishmentDate(LocalDate establishmentDate) {
        this.establishmentDate = establishmentDate;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}