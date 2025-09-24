package org.jbd.backend.user.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CertificationCreateDto {
    
    @NotBlank(message = "자격증명은 필수입니다.")
    private String certificationName;
    
    @NotBlank(message = "발급기관은 필수입니다.")
    private String issuingOrganization;
    
    @NotNull(message = "발급일은 필수입니다.")
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
    private String credentialId;
    private String credentialUrl;
    private String description;
    
    protected CertificationCreateDto() {}
    
    // Getters
    public String getCertificationName() { return certificationName; }
    public String getIssuingOrganization() { return issuingOrganization; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getCredentialId() { return credentialId; }
    public String getCredentialUrl() { return credentialUrl; }
    public String getDescription() { return description; }
}