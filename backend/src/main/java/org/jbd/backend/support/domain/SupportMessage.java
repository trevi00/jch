package org.jbd.backend.support.domain;

import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.User;

@Entity
@Table(name = "support_messages")
public class SupportMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket supportTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;

    @Column(name = "is_from_admin", nullable = false)
    private Boolean isFromAdmin = false;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "is_internal_note", nullable = false)
    private Boolean isInternalNote = false; // For admin-only notes

    protected SupportMessage() {}

    public SupportMessage(SupportTicket supportTicket, User sender, String messageContent, Boolean isFromAdmin) {
        this.supportTicket = supportTicket;
        this.sender = sender;
        this.messageContent = messageContent;
        this.isFromAdmin = isFromAdmin != null ? isFromAdmin : false;
        this.isInternalNote = false;
    }

    public SupportMessage(SupportTicket supportTicket, User sender, String messageContent, Boolean isFromAdmin, String attachmentUrl) {
        this(supportTicket, sender, messageContent, isFromAdmin);
        this.attachmentUrl = attachmentUrl;
    }

    public void markAsInternalNote() {
        this.isInternalNote = true;
    }

    public void addAttachment(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public boolean isFromAdmin() {
        return Boolean.TRUE.equals(isFromAdmin);
    }

    public boolean isFromUser() {
        return !isFromAdmin();
    }

    public boolean isInternalNote() {
        return Boolean.TRUE.equals(isInternalNote);
    }

    public boolean hasAttachment() {
        return this.attachmentUrl != null && !this.attachmentUrl.trim().isEmpty();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public SupportTicket getSupportTicket() {
        return supportTicket;
    }

    public User getSender() {
        return sender;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public Boolean getIsFromAdmin() {
        return isFromAdmin;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public Boolean getIsInternalNote() {
        return isInternalNote;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setSupportTicket(SupportTicket supportTicket) {
        this.supportTicket = supportTicket;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public void setIsFromAdmin(Boolean isFromAdmin) {
        this.isFromAdmin = isFromAdmin;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public void setIsInternalNote(Boolean isInternalNote) {
        this.isInternalNote = isInternalNote;
    }
}