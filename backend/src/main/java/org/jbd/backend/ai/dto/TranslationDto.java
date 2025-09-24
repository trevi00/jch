package org.jbd.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class TranslationDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslateRequest {
        private String text;
        @JsonProperty("target_language")
        private String targetLanguage; // "ko", "en", "ja", "zh"
        @JsonProperty("source_language")
        private String sourceLanguage; // Optional, auto-detect if not provided
        @JsonProperty("document_type")
        private String documentType; // "email", "resume", "cover_letter", "general"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslateResponse {
        private boolean success;
        private String message;
        private TranslationData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslationData {
        private Translation translation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Translation {
        @JsonProperty("translated_text")
        private String translatedText;
        @JsonProperty("original_text")
        private String originalText;
        @JsonProperty("source_language")
        private String sourceLanguage;
        @JsonProperty("target_language")
        private String targetLanguage;
        @JsonProperty("document_type")
        private String documentType;
        @JsonProperty("created_at")
        private String createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchTranslateRequest {
        private List<String> texts;
        @JsonProperty("target_language")
        private String targetLanguage;
        @JsonProperty("source_language")
        private String sourceLanguage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchTranslateResponse {
        private boolean success;
        private String message;
        private BatchTranslationData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchTranslationData {
        private List<TranslationResult> translations;
        @JsonProperty("total_count")
        private int totalCount;
        @JsonProperty("response_time")
        private double responseTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslationResult {
        @JsonProperty("original_text")
        private String originalText;
        @JsonProperty("translated_text")
        private String translatedText;
        private double confidence;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Template {
        private String name;
        private String description;
        @JsonProperty("source_language")
        private String sourceLanguage;
        @JsonProperty("target_language")
        private String targetLanguage;
        private String template;
    }
}