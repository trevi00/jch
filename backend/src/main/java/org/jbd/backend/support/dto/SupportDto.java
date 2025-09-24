package org.jbd.backend.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SupportDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTicketRequest {
        @NotBlank(message = "제목은 필수입니다")
        private String subject;

        @NotBlank(message = "내용은 필수입니다")
        private String description;

        @NotBlank(message = "카테고리는 필수입니다")
        private String category;

        @NotBlank(message = "우선순위는 필수입니다")
        private String priority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketResponse {
        private Long id;
        private String subject;
        private String description;
        private String category;
        private String priority;
        private String status;
        private String assignedAdminName;
        private String createdAt;
        private String updatedAt;
        private String resolvedAt;
        private Integer messageCount;
        private Integer satisfactionRating;
        private String satisfactionFeedback;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMessageRequest {
        @NotBlank(message = "메시지는 필수입니다")
        private String message;

        private String attachmentUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private Long id;
        private Long ticketId;
        private String senderName;
        private String message;
        private boolean isFromSupport;
        private boolean isInternalNote;
        private String attachmentUrl;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotBlank(message = "상태는 필수입니다")
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SatisfactionRequest {
        @NotNull(message = "평점은 필수입니다")
        private Integer rating;

        private String feedback;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketStats {
        private Long totalTickets;
        private Long openTickets;
        private Long inProgressTickets;
        private Long resolvedTickets;
        private Long closedTickets;
        private Long averageResponseTime;
        private Double satisfactionScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResponse {
        private String value;
        private String label;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FAQResponse {
        private Long id;
        private String question;
        private String answer;
        private String category;
        private Integer viewCount;
        private Integer helpful;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileUploadResponse {
        private String fileUrl;
        private String fileName;
        private Long fileSize;
    }
}