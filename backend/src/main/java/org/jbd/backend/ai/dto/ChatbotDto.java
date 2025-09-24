package org.jbd.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatbotDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        @JsonProperty("user_id")
        private String userId;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        private boolean success;
        private String message;
        private ChatData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatData {
        private String response;
        private String category;
        private double confidence;
        @JsonProperty("response_time")
        private double responseTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickResponseRequest {
        @JsonProperty("user_id")
        private String userId;
        private String category;
    }
}