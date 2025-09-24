package org.jbd.backend.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.ai.dto.TranslationDto;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AITranslationService {

    private final AIServiceClient aiServiceClient;
    
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
        "ko", "en", "ja", "zh", "es", "fr", "de", "ru", "pt", "it"
    );

    /**
     * 텍스트 번역 서비스
     */
    public TranslationDto.TranslateResponse translateText(
            String text, String targetLanguage, String sourceLanguage) {
        return translateText(text, targetLanguage, sourceLanguage, "general");
    }
    
    /**
     * 텍스트 번역 서비스 (문서 타입 지정)
     */
    public TranslationDto.TranslateResponse translateText(
            String text, String targetLanguage, String sourceLanguage, String documentType) {
        
        log.info("Translating text from {} to {} (document type: {})", 
                sourceLanguage != null ? sourceLanguage : "auto-detect", targetLanguage, documentType);
        
        // 입력값 유효성 검증
        if (text == null || text.trim().isEmpty()) {
            return createValidationErrorResponse("번역할 텍스트를 입력해주세요.");
        }

        if (text.length() > 5000) {
            return createValidationErrorResponse("번역할 텍스트는 5000자 이하로 입력해주세요.");
        }

        if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
            return createValidationErrorResponse("번역할 언어를 선택해주세요.");
        }

        if (!SUPPORTED_LANGUAGES.contains(targetLanguage.toLowerCase())) {
            return createValidationErrorResponse("지원하지 않는 번역 언어입니다. 지원 언어: " + String.join(", ", SUPPORTED_LANGUAGES));
        }

        // 소스 언어와 타겟 언어가 같은 경우 체크
        if (sourceLanguage != null && sourceLanguage.equalsIgnoreCase(targetLanguage)) {
            return createSameLanguageResponse(text.trim(), targetLanguage);
        }

        return aiServiceClient.translateText(
            text.trim(), 
            targetLanguage.toLowerCase(), 
            sourceLanguage != null ? sourceLanguage.toLowerCase() : null,
            documentType
        );
    }

    /**
     * 이력서/자소서 전용 번역 서비스 (전문 용어 고려)
     */
    public TranslationDto.TranslateResponse translateResumeContent(
            String content, String targetLanguage) {
        
        log.info("Translating resume content to {}", targetLanguage);
        
        if (content == null || content.trim().isEmpty()) {
            return createValidationErrorResponse("번역할 내용을 입력해주세요.");
        }

        // 이력서/자소서 관련 전문 용어가 포함된 경우 추가 처리
        String enhancedContent = preprocessResumeContent(content);
        
        return translateText(enhancedContent, targetLanguage, null);
    }

    /**
     * 면접 질문/답변 번역 서비스
     */
    public TranslationDto.TranslateResponse translateInterviewContent(
            String content, String targetLanguage) {
        
        log.info("Translating interview content to {}", targetLanguage);
        
        if (content == null || content.trim().isEmpty()) {
            return createValidationErrorResponse("번역할 면접 내용을 입력해주세요.");
        }

        // 면접 관련 전문 용어 전처리
        String enhancedContent = preprocessInterviewContent(content);
        
        return translateText(enhancedContent, targetLanguage, null);
    }

    /**
     * 지원되는 언어 목록 조회
     */
    public Set<String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    /**
     * 언어 코드를 한국어 이름으로 변환
     */
    public String getLanguageName(String languageCode) {
        return switch (languageCode.toLowerCase()) {
            case "ko" -> "한국어";
            case "en" -> "영어";
            case "ja" -> "일본어";
            case "zh" -> "중국어";
            case "es" -> "스페인어";
            case "fr" -> "프랑스어";
            case "de" -> "독일어";
            case "ru" -> "러시아어";
            case "pt" -> "포르투갈어";
            case "it" -> "이탈리아어";
            default -> "알 수 없는 언어";
        };
    }

    /**
     * 번역 품질 평가
     */
    public String evaluateTranslationQuality(String originalText, String translatedText, 
            String sourceLanguage, String targetLanguage) {
        
        // 간단한 품질 평가 로직
        if (translatedText.length() < originalText.length() * 0.3) {
            return "번역 결과가 너무 짧아 품질이 낮을 수 있습니다.";
        }
        
        if (translatedText.equals(originalText)) {
            return "원본과 동일한 텍스트입니다. 언어 설정을 확인해주세요.";
        }
        
        return "번역이 완료되었습니다.";
    }

    /**
     * 이력서 내용 전처리 (전문 용어 보존)
     */
    private String preprocessResumeContent(String content) {
        // 이력서 관련 전문 용어들을 보호하기 위한 전처리
        // 실제로는 더 정교한 전문 용어 사전을 활용해야 함
        return content
            .replace("프론트엔드", "Frontend")
            .replace("백엔드", "Backend")
            .replace("풀스택", "Full-stack")
            .replace("데브옵스", "DevOps")
            .replace("머신러닝", "Machine Learning")
            .replace("인공지능", "AI");
    }

    /**
     * 면접 내용 전처리
     */
    private String preprocessInterviewContent(String content) {
        // 면접 관련 전문 용어 전처리
        return content
            .replace("자기소개", "self-introduction")
            .replace("지원동기", "motivation for application")
            .replace("장단점", "strengths and weaknesses")
            .replace("프로젝트 경험", "project experience")
            .replace("팀워크", "teamwork");
    }

    /**
     * 동일 언어 번역 시 응답
     */
    private TranslationDto.TranslateResponse createSameLanguageResponse(String text, String language) {
        TranslationDto.Translation translation = new TranslationDto.Translation(
            text, text, language, language, "general", ""
        );
        TranslationDto.TranslationData data = new TranslationDto.TranslationData(translation);
        return new TranslationDto.TranslateResponse(
            true, 
            "소스 언어와 타겟 언어가 동일합니다.", 
            data
        );
    }

    /**
     * 유효성 검증 오류 응답 생성
     */
    private TranslationDto.TranslateResponse createValidationErrorResponse(String message) {
        return new TranslationDto.TranslateResponse(false, message, null);
    }
}