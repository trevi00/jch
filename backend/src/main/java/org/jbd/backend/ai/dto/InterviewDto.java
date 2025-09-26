package org.jbd.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class InterviewDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateQuestionsRequest {
        private String position;
        @JsonProperty("interview_type")
        private String interviewType; // "technical", "behavioral", "general"
        private int count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateQuestionsResponse {
        private boolean success;
        private String message;
        private QuestionsData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionsData {
        private List<Question> questions;
        private String position;
        @JsonProperty("interview_type")
        private String interviewType;
        @JsonProperty("response_time")
        private double responseTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question {
        private String question;
        private String category;
        private String difficulty;
        @JsonProperty("expected_keywords")
        private List<String> expectedKeywords;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluateAnswerRequest {
        private String question;
        private String answer;
        private String position;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluateAnswerResponse {
        private boolean success;
        private String message;
        private EvaluationData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationData {
        private int score;
        private String feedback;
        private List<String> strengths;
        private List<String> improvements;
        @JsonProperty("keyword_match")
        private double keywordMatch;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteInterviewRequest {
        @JsonProperty("job_role")
        @JsonAlias({"jobRole", "job_role"})
        private String jobRole;
        private List<QuestionData> questions;
        private List<AnswerData> answers;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionData {
        private String id;
        private String question;
        private String type;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerData {
        @JsonProperty("question_id")
        @JsonAlias({"questionId", "question_id"})
        private String questionId;
        private String answer;
        private FeedbackData feedback;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackData {
        private Integer score;
        private List<String> strengths;
        private List<String> improvements;
        private String suggestion;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteInterviewResponse {
        private boolean success;
        private String message;
        private InterviewResultData data;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewResultData {
        @JsonProperty("interview_id")
        private Long interviewId;
        @JsonProperty("overall_score")
        private Double overallScore;
        @JsonProperty("total_questions")
        private Integer totalQuestions;
        @JsonProperty("answered_questions")
        private Integer answeredQuestions;
        @JsonProperty("completed_at")
        private String completedAt;
    }
}