package org.jbd.backend.user.domain;

import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.enums.CertificationStatus;

import java.time.LocalDate;

@Entity
@Table(name = "certifications")
public class Certifications extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "certification_name", nullable = false)
    private String certificationName;

    @Column(name = "issuing_organization", nullable = false)
    private String issuingOrganization;

    @Column(name = "certification_number")
    private String certificationNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "is_lifetime_valid")
    private Boolean isLifetimeValid = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CertificationStatus status = CertificationStatus.ACTIVE;

    @Column(name = "score")
    private String score;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "verification_url")
    private String verificationUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "related_skills", columnDefinition = "TEXT")
    private String relatedSkills;

    public Certifications() {}

    public Certifications(User user, String certificationName, String issuingOrganization) {
        this.user = user;
        this.certificationName = certificationName;
        this.issuingOrganization = issuingOrganization;
    }

    // Business methods
    public void updateCertificationInfo(String certificationName, String issuingOrganization) {
        this.certificationName = certificationName;
        this.issuingOrganization = issuingOrganization;
    }

    public void updateValidityPeriod(LocalDate issueDate, LocalDate expirationDate, Boolean isLifetimeValid) {
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.isLifetimeValid = isLifetimeValid;
    }

    public void updateScore(String score) {
        this.score = score;
    }

    public void addCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public void addVerificationUrl(String verificationUrl) {
        this.verificationUrl = verificationUrl;
    }

    public void expire() {
        this.status = CertificationStatus.EXPIRED;
    }

    public void revoke() {
        this.status = CertificationStatus.REVOKED;
    }

    public void reactivate() {
        this.status = CertificationStatus.ACTIVE;
    }

    public boolean isLifetimeValid() {
        return Boolean.TRUE.equals(isLifetimeValid);
    }

    public boolean isExpired() {
        if (isLifetimeValid()) {
            return false;
        }
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return status == CertificationStatus.ACTIVE && !isExpired();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCertificationName() {
        return certificationName;
    }

    public void setCertificationName(String certificationName) {
        this.certificationName = certificationName;
    }

    public String getIssuingOrganization() {
        return issuingOrganization;
    }

    public void setIssuingOrganization(String issuingOrganization) {
        this.issuingOrganization = issuingOrganization;
    }

    public String getCertificationNumber() {
        return certificationNumber;
    }

    public void setCertificationNumber(String certificationNumber) {
        this.certificationNumber = certificationNumber;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Boolean getIsLifetimeValid() {
        return isLifetimeValid;
    }

    public void setIsLifetimeValid(Boolean isLifetimeValid) {
        this.isLifetimeValid = isLifetimeValid;
    }

    public CertificationStatus getStatus() {
        return status;
    }

    public void setStatus(CertificationStatus status) {
        this.status = status;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public void setVerificationUrl(String verificationUrl) {
        this.verificationUrl = verificationUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRelatedSkills() {
        return relatedSkills;
    }

    public void setRelatedSkills(String relatedSkills) {
        this.relatedSkills = relatedSkills;
    }
}