package org.jbd.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class CoverLetterDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateCompleteRequest {
        private String company;
        private String position;
        @JsonProperty("user_experience")
        private String userExperience;
        @JsonProperty("additional_info")
        private String additionalInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateCompleteResponse {
        private boolean success;
        private String message;
        private CoverLetterData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoverLetterData {
        @JsonProperty("cover_letter")
        private String coverLetter;
        private List<Section> sections;
        @JsonProperty("word_count")
        private int wordCount;
        @JsonProperty("response_time")
        private double responseTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private String title;
        private String content;
        private String type; // "introduction", "experience", "motivation", "conclusion"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateSectionRequest {
        private String company;
        private String position;
        @JsonProperty("section_type")
        private String sectionType;
        @JsonProperty("user_experience")
        private String userExperience;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackRequest {
        @JsonProperty("cover_letter_text")
        private String coverLetterText;
        private String position;
        private String company;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackResponse {
        private boolean success;
        private String message;
        private FeedbackData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackData {
        @JsonProperty("overall_score")
        private int overallScore;
        private String feedback;
        private List<String> strengths;
        private List<String> improvements;
        @JsonProperty("grammar_score")
        private int grammarScore;
        @JsonProperty("content_score")
        private int contentScore;
    }
}