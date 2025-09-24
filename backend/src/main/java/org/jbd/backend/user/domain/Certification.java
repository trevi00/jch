package org.jbd.backend.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "user_certifications")
public class Certification extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "certification_name", nullable = false)
    private String certificationName;
    
    @Column(name = "issuing_organization", nullable = false)
    private String issuingOrganization;
    
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "credential_id")
    private String credentialId;
    
    @Column(name = "credential_url")
    private String credentialUrl;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    protected Certification() {}
    
    public Certification(User user, String certificationName, String issuingOrganization, LocalDate issueDate) {
        this.user = user;
        this.certificationName = certificationName;
        this.issuingOrganization = issuingOrganization;
        this.issueDate = issueDate;
    }
    
    public void updateCertification(String certificationName, String issuingOrganization, LocalDate issueDate,
                                   LocalDate expiryDate, String credentialId, String credentialUrl, String description) {
        this.certificationName = certificationName;
        this.issuingOrganization = issuingOrganization;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.credentialId = credentialId;
        this.credentialUrl = credentialUrl;
        this.description = description;
    }
    
    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getCertificationName() {
        return certificationName;
    }
    
    public String getIssuingOrganization() {
        return issuingOrganization;
    }
    
    public LocalDate getIssueDate() {
        return issueDate;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public String getCredentialId() {
        return credentialId;
    }
    
    public String getCredentialUrl() {
        return credentialUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
}