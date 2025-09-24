package org.jbd.backend.ai.domain;

import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "translation_requests")
public class TranslationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "translation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "source_language", nullable = false, length = 10)
    private String sourceLanguage;

    @Column(name = "target_language", nullable = false, length = 10)
    private String targetLanguage;

    @Column(name = "source_text", nullable = false, columnDefinition = "TEXT")
    private String sourceText;

    @Column(name = "translated_text", columnDefinition = "TEXT")
    private String translatedText;

    @Column(name = "document_type", length = 50)
    private String documentType; // "resume", "cover_letter", "general"

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TranslationStatus status = TranslationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "character_count")
    private Integer characterCount;

    @Column(name = "cost")
    private Double cost;

    protected TranslationRequest() {}

    public TranslationRequest(User user, String sourceLanguage, String targetLanguage, String sourceText, String documentType) {
        this.user = user;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sourceText = sourceText;
        this.documentType = documentType;
        this.status = TranslationStatus.PENDING;
        this.characterCount = sourceText != null ? sourceText.length() : 0;
    }

    public void markAsInProgress() {
        this.status = TranslationStatus.IN_PROGRESS;
    }

    public void markAsCompleted(String translatedText) {
        this.status = TranslationStatus.COMPLETED;
        this.translatedText = translatedText;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = TranslationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public boolean isCompleted() {
        return this.status == TranslationStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == TranslationStatus.FAILED;
    }

    public boolean isPending() {
        return this.status == TranslationStatus.PENDING;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public String getDocumentType() {
        return documentType;
    }

    public TranslationStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public Integer getCharacterCount() {
        return characterCount;
    }

    public Double getCost() {
        return cost;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
        this.characterCount = sourceText != null ? sourceText.length() : 0;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setStatus(TranslationStatus status) {
        this.status = status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public void setCharacterCount(Integer characterCount) {
        this.characterCount = characterCount;
    }
}

enum TranslationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}