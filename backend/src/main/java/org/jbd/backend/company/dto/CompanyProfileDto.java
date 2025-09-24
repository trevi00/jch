package org.jbd.backend.company.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.jbd.backend.company.domain.Company;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CompanyProfileDto {
    
    private Long id;
    private String companyName;
    private String businessNumber;
    private String industry;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate establishmentDate;
    private Integer employeeCount;
    private BigDecimal revenue;
    private String description;
    private String websiteUrl;
    private String companyEmail;
    private String phoneNumber;
    private String address;
    private String logoUrl;
    private Boolean isVerified;

    public CompanyProfileDto() {}

    public CompanyProfileDto(Company company) {
        this.id = company.getId();
        this.companyName = company.getCompanyName();
        this.businessNumber = company.getBusinessNumber();
        this.industry = company.getIndustry();
        this.location = company.getLocation();
        this.establishmentDate = company.getEstablishmentDate();
        this.employeeCount = company.getEmployeeCount();
        this.revenue = company.getRevenue();
        this.description = company.getDescription();
        this.websiteUrl = company.getWebsiteUrl();
        this.companyEmail = company.getCompanyEmail();
        this.phoneNumber = company.getPhoneNumber();
        this.address = company.getAddress();
        this.logoUrl = company.getLogoUrl();
        this.isVerified = company.getIsVerified();
    }

    // Getters
    public Long getId() {
        return id;
    }

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

    public Boolean getIsVerified() {
        return isVerified;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

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

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
}