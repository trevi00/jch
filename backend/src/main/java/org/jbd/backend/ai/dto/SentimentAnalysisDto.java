package org.jbd.backend.ai.dto;

public class SentimentAnalysisDto {
    
    public record AnalyzeRequest(
            String text,
            String language
    ) {}
    
    public record AnalyzeResponse(
            boolean success,
            String message,
            SentimentData data
    ) {}
    
    public record SentimentData(
            String label,        // POSITIVE, NEGATIVE, NEUTRAL
            double score,        // -1.0 to 1.0
            double confidence,   // 0.0 to 1.0
            String explanation
    ) {}
}