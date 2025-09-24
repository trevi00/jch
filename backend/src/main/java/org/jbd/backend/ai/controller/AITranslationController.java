package org.jbd.backend.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jbd.backend.ai.dto.TranslationDto;
import org.jbd.backend.ai.service.AITranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/ai/translation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AITranslationController {

    private final AITranslationService aiTranslationService;

    @PostMapping("/translate")
    public ResponseEntity<TranslationDto.TranslateResponse> translateText(
            @Valid @RequestBody TranslateRequest request) {
        
        TranslationDto.TranslateResponse response = aiTranslationService.translateText(
            request.getText(),
            request.getTargetLanguage(),
            request.getSourceLanguage()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate/resume")
    public ResponseEntity<TranslationDto.TranslateResponse> translateResumeContent(
            @Valid @RequestBody ResumeTranslateRequest request) {
        
        TranslationDto.TranslateResponse response = aiTranslationService.translateResumeContent(
            request.getContent(),
            request.getTargetLanguage()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate/interview")
    public ResponseEntity<TranslationDto.TranslateResponse> translateInterviewContent(
            @Valid @RequestBody InterviewTranslateRequest request) {
        
        TranslationDto.TranslateResponse response = aiTranslationService.translateInterviewContent(
            request.getContent(),
            request.getTargetLanguage()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/languages")
    public ResponseEntity<SupportedLanguagesResponse> getSupportedLanguages() {
        Set<String> languages = aiTranslationService.getSupportedLanguages();
        SupportedLanguagesResponse response = new SupportedLanguagesResponse(languages);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/language-name")
    public ResponseEntity<LanguageNameResponse> getLanguageName(@RequestParam String languageCode) {
        String languageName = aiTranslationService.getLanguageName(languageCode);
        LanguageNameResponse response = new LanguageNameResponse(languageCode, languageName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<TranslationQualityResponse> evaluateTranslationQuality(
            @Valid @RequestBody EvaluationRequest request) {
        
        String evaluation = aiTranslationService.evaluateTranslationQuality(
            request.getOriginalText(),
            request.getTranslatedText(),
            request.getSourceLanguage(),
            request.getTargetLanguage()
        );
        
        TranslationQualityResponse response = new TranslationQualityResponse(evaluation);
        return ResponseEntity.ok(response);
    }

    // Request DTOs
    public static class TranslateRequest {
        private String text;
        private String targetLanguage;
        private String sourceLanguage;

        public TranslateRequest() {}

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTargetLanguage() {
            return targetLanguage;
        }

        public void setTargetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
        }

        public String getSourceLanguage() {
            return sourceLanguage;
        }

        public void setSourceLanguage(String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
        }
    }

    public static class ResumeTranslateRequest {
        private String content;
        private String targetLanguage;

        public ResumeTranslateRequest() {}

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTargetLanguage() {
            return targetLanguage;
        }

        public void setTargetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
        }
    }

    public static class InterviewTranslateRequest {
        private String content;
        private String targetLanguage;

        public InterviewTranslateRequest() {}

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTargetLanguage() {
            return targetLanguage;
        }

        public void setTargetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
        }
    }

    public static class EvaluationRequest {
        private String originalText;
        private String translatedText;
        private String sourceLanguage;
        private String targetLanguage;

        public EvaluationRequest() {}

        public String getOriginalText() {
            return originalText;
        }

        public void setOriginalText(String originalText) {
            this.originalText = originalText;
        }

        public String getTranslatedText() {
            return translatedText;
        }

        public void setTranslatedText(String translatedText) {
            this.translatedText = translatedText;
        }

        public String getSourceLanguage() {
            return sourceLanguage;
        }

        public void setSourceLanguage(String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
        }

        public String getTargetLanguage() {
            return targetLanguage;
        }

        public void setTargetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
        }
    }

    // Response DTOs
    public static class SupportedLanguagesResponse {
        private Set<String> languages;

        public SupportedLanguagesResponse(Set<String> languages) {
            this.languages = languages;
        }

        public Set<String> getLanguages() {
            return languages;
        }
    }

    public static class LanguageNameResponse {
        private String languageCode;
        private String languageName;

        public LanguageNameResponse(String languageCode, String languageName) {
            this.languageCode = languageCode;
            this.languageName = languageName;
        }

        public String getLanguageCode() {
            return languageCode;
        }

        public String getLanguageName() {
            return languageName;
        }
    }

    public static class TranslationQualityResponse {
        private String evaluation;

        public TranslationQualityResponse(String evaluation) {
            this.evaluation = evaluation;
        }

        public String getEvaluation() {
            return evaluation;
        }
    }
}