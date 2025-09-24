package org.jbd.backend.webmail.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jbd.backend.common.entity.BaseEntity;

@Entity
@Table(name = "email_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailHistory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sender_email", nullable = false)
    private String senderEmail;
    
    @Column(name = "sender_name")
    private String senderName;
    
    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;
    
    @Column(name = "subject", nullable = false, length = 200)
    private String subject;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;
    
    @Column(name = "translated_content", columnDefinition = "TEXT")
    private String translatedContent;
    
    @Builder.Default
    @Column(name = "was_translated")
    private boolean wasTranslated = false;
    
    @Column(name = "source_language", length = 10)
    private String sourceLanguage;
    
    @Column(name = "target_language", length = 10)
    private String targetLanguage;
    
    @Column(name = "document_type", length = 50)
    private String documentType;
    
    @Column(name = "sendgrid_message_id")
    private String sendgridMessageId;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmailStatus status = EmailStatus.SENT;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
}