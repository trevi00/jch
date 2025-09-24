package org.jbd.backend.support.domain;

import jakarta.persistence.*;
import org.jbd.backend.common.entity.BaseEntity;
import org.jbd.backend.user.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_tickets")
public class SupportTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_admin_id")
    private User assignedAdmin;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // "account", "technical", "payment", "general"

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating; // 1-5 stars

    @Column(name = "satisfaction_feedback", columnDefinition = "TEXT")
    private String satisfactionFeedback;

    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupportMessage> messages = new ArrayList<>();

    protected SupportTicket() {}

    public SupportTicket(User user, String title, String description, String category, TicketPriority priority) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority != null ? priority : TicketPriority.MEDIUM;
        this.status = TicketStatus.OPEN;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void assignToAdmin(User admin) {
        this.assignedAdmin = admin;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void markInProgress() {
        if (this.status == TicketStatus.OPEN) {
            this.status = TicketStatus.IN_PROGRESS;
            this.lastActivityAt = LocalDateTime.now();
        }
    }

    public void markResolved() {
        if (this.status == TicketStatus.IN_PROGRESS) {
            this.status = TicketStatus.RESOLVED;
            this.resolvedAt = LocalDateTime.now();
            this.lastActivityAt = LocalDateTime.now();
        }
    }

    public void close() {
        this.status = TicketStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    public void reopen() {
        if (this.status == TicketStatus.RESOLVED || this.status == TicketStatus.CLOSED) {
            this.status = TicketStatus.OPEN;
            this.resolvedAt = null;
            this.closedAt = null;
            this.lastActivityAt = LocalDateTime.now();
        }
    }

    public void addMessage(SupportMessage message) {
        messages.add(message);
        message.setSupportTicket(this);
        this.lastActivityAt = LocalDateTime.now();

        // Set first response time if this is the first admin response
        if (message.isFromAdmin() && this.firstResponseAt == null) {
            this.firstResponseAt = LocalDateTime.now();
        }
    }

    public void removeMessage(SupportMessage message) {
        messages.remove(message);
        message.setSupportTicket(null);
    }

    public void addSatisfactionRating(Integer rating, String feedback) {
        if (this.status == TicketStatus.RESOLVED || this.status == TicketStatus.CLOSED) {
            this.satisfactionRating = rating;
            this.satisfactionFeedback = feedback;
        }
    }

    public boolean isOpen() {
        return this.status == TicketStatus.OPEN;
    }

    public boolean isInProgress() {
        return this.status == TicketStatus.IN_PROGRESS;
    }

    public boolean isResolved() {
        return this.status == TicketStatus.RESOLVED;
    }

    public boolean isClosed() {
        return this.status == TicketStatus.CLOSED;
    }

    public boolean canBeModifiedByUser() {
        return this.status == TicketStatus.OPEN || this.status == TicketStatus.IN_PROGRESS;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public User getAssignedAdmin() {
        return assignedAdmin;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public LocalDateTime getFirstResponseAt() {
        return firstResponseAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public Integer getSatisfactionRating() {
        return satisfactionRating;
    }

    public String getSatisfactionFeedback() {
        return satisfactionFeedback;
    }

    public List<SupportMessage> getMessages() {
        return messages;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAssignedAdmin(User assignedAdmin) {
        this.assignedAdmin = assignedAdmin;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public void setFirstResponseAt(LocalDateTime firstResponseAt) {
        this.firstResponseAt = firstResponseAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public void setSatisfactionRating(Integer satisfactionRating) {
        this.satisfactionRating = satisfactionRating;
    }

    public void setSatisfactionFeedback(String satisfactionFeedback) {
        this.satisfactionFeedback = satisfactionFeedback;
    }

    public void setMessages(List<SupportMessage> messages) {
        this.messages = messages;
    }
}