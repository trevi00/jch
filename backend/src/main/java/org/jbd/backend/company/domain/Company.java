package org.jbd.backend.company.domain;

import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "business_number", unique = true, length = 12)
    private String businessNumber;

    @Column(name = "industry", length = 50)
    private String industry;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "establishment_date")
    private LocalDate establishmentDate;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "revenue", precision = 15, scale = 2)
    private BigDecimal revenue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "website_url", length = 200)
    private String websiteUrl;

    @Column(name = "company_email", length = 100)
    private String companyEmail;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    public Company() {}

    public Company(User user, String companyName) {
        this.user = user;
        this.companyName = companyName;
    }

    // Business Methods
    public void updateBasicInfo(String companyName, String industry, String location, String description) {
        this.companyName = companyName;
        this.industry = industry;
        this.location = location;
        this.description = description;
    }

    public void updateBusinessInfo(String businessNumber, LocalDate establishmentDate, 
                                 Integer employeeCount, BigDecimal revenue) {
        this.businessNumber = businessNumber;
        this.establishmentDate = establishmentDate;
        this.employeeCount = employeeCount;
        this.revenue = revenue;
    }

    public void updateContactInfo(String companyEmail, String phoneNumber, String address, String websiteUrl) {
        this.companyEmail = companyEmail;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.websiteUrl = websiteUrl;
    }

    public void verify() {
        this.isVerified = true;
    }

    public void unverify() {
        this.isVerified = false;
    }

    public boolean isVerified() {
        return Boolean.TRUE.equals(isVerified);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
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

    public void setUser(User user) {
        this.user = user;
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