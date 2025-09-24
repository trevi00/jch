package org.jbd.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ImageGenerationDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateRequest {
        private String prompt;
        private String style; // "professional", "casual", "creative", "modern"
        private String size; // "256x256", "512x512", "1024x1024"
        private int n; // Number of images to generate (default: 1)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateResponse {
        private boolean success;
        private String message;
        private ImageData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageData {
        @JsonProperty("image_url")
        private String imageUrl;
        private String prompt;
        private String style;
        private String size;
        @JsonProperty("response_time")
        private double responseTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateWithSentimentRequest {
        private String text;
        private String style;
        private String size;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateVariationsRequest {
        @JsonProperty("base_prompt")
        private String basePrompt;
        private int variations; // Number of variations (default: 3)
        private String style;
        private String size;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateVariationsResponse {
        private boolean success;
        private String message;
        private VariationsData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariationsData {
        private List<ImageVariation> variations;
        @JsonProperty("base_prompt")
        private String basePrompt;
        @JsonProperty("response_time")
        private double responseTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageVariation {
        @JsonProperty("image_url")
        private String imageUrl;
        private String prompt;
        @JsonProperty("variation_type")
        private String variationType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyzeSentimentRequest {
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyzeSentimentResponse {
        private boolean success;
        private String message;
        private SentimentData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentimentData {
        private String sentiment; // "positive", "negative", "neutral"
        private double confidence;
        private List<String> emotions;
        @JsonProperty("suggested_style")
        private String suggestedStyle;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StyleInfo {
        private String name;
        private String description;
        @JsonProperty("example_prompt")
        private String examplePrompt;
    }
}