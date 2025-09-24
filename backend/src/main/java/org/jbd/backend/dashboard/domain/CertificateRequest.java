package org.jbd.backend.dashboard.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jbd.backend.user.domain.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CertificateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateType certificateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private org.jbd.backend.dashboard.domain.enums.RequestStatus status;

    @Column(nullable = false)
    private String purpose;

    private String adminNotes;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime processedAt;

    public CertificateRequest(User user, CertificateType certificateType, String purpose) {
        this.user = user;
        this.certificateType = certificateType;
        this.purpose = purpose;
        this.status = org.jbd.backend.dashboard.domain.enums.RequestStatus.PENDING;
    }

    public void approve(String adminNotes) {
        this.status = org.jbd.backend.dashboard.domain.enums.RequestStatus.APPROVED;
        this.adminNotes = adminNotes;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(String adminNotes) {
        this.status = org.jbd.backend.dashboard.domain.enums.RequestStatus.REJECTED;
        this.adminNotes = adminNotes;
        this.processedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != org.jbd.backend.dashboard.domain.enums.RequestStatus.APPROVED) {
            throw new IllegalStateException("승인되지 않은 요청은 완료할 수 없습니다.");
        }
        this.status = org.jbd.backend.dashboard.domain.enums.RequestStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == org.jbd.backend.dashboard.domain.enums.RequestStatus.PENDING;
    }

    public boolean isProcessed() {
        return this.status != org.jbd.backend.dashboard.domain.enums.RequestStatus.PENDING;
    }
}