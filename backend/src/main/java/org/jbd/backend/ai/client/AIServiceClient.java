package org.jbd.backend.ai.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.ai.config.AIServiceConfig;
import org.jbd.backend.ai.dto.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIServiceClient {

    private final RestTemplate restTemplate;
    private final AIServiceConfig aiServiceConfig;

    // ============= Chatbot Services =============
    
    public ChatbotDto.ChatResponse chatWithBot(String userId, String message) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/chatbot/chat";
            ChatbotDto.ChatRequest request = new ChatbotDto.ChatRequest(userId, message);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ChatbotDto.ChatRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ChatbotDto.ChatResponse> response = restTemplate.postForEntity(
                url, entity, ChatbotDto.ChatResponse.class
            );
            
            log.info("Chatbot API called successfully for user: {}", userId);
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to call chatbot API: {}", e.getMessage());
            return createErrorChatResponse("챗봇 서비스와 통신 중 오류가 발생했습니다.");
        }
    }

    public ChatbotDto.ChatResponse getQuickResponse(String userId, String category) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/chatbot/quick-response";
            ChatbotDto.QuickResponseRequest request = new ChatbotDto.QuickResponseRequest(userId, category);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ChatbotDto.QuickResponseRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ChatbotDto.ChatResponse> response = restTemplate.postForEntity(
                url, entity, ChatbotDto.ChatResponse.class
            );
            
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to get quick response: {}", e.getMessage());
            return createErrorChatResponse("빠른 응답 서비스와 통신 중 오류가 발생했습니다.");
        }
    }

    // ============= Interview Services =============
    
    public InterviewDto.GenerateQuestionsResponse generateInterviewQuestions(
            String position, String interviewType, int count) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/interview/generate-questions";
            InterviewDto.GenerateQuestionsRequest request = 
                new InterviewDto.GenerateQuestionsRequest(position, interviewType, count);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<InterviewDto.GenerateQuestionsRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<InterviewDto.GenerateQuestionsResponse> response = restTemplate.postForEntity(
                url, entity, InterviewDto.GenerateQuestionsResponse.class
            );
            
            log.info("Interview questions generated for position: {}, type: {}", position, interviewType);
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to generate interview questions: {}", e.getMessage());
            return createErrorInterviewResponse("면접 질문 생성 중 오류가 발생했습니다.");
        }
    }

    public InterviewDto.EvaluateAnswerResponse evaluateAnswer(
            String question, String answer, String position) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/interview/evaluate-answer";
            InterviewDto.EvaluateAnswerRequest request = 
                new InterviewDto.EvaluateAnswerRequest(question, answer, position);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<InterviewDto.EvaluateAnswerRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<InterviewDto.EvaluateAnswerResponse> response = restTemplate.postForEntity(
                url, entity, InterviewDto.EvaluateAnswerResponse.class
            );
            
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to evaluate answer: {}", e.getMessage());
            return createErrorEvaluationResponse("답변 평가 중 오류가 발생했습니다.");
        }
    }

    // ============= Cover Letter Services =============
    
    public CoverLetterDto.GenerateCompleteResponse generateCompleteCoverLetter(
            String company, String position, String userExperience, String additionalInfo) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/cover-letter/generate-complete";
            CoverLetterDto.GenerateCompleteRequest request = 
                new CoverLetterDto.GenerateCompleteRequest(company, position, userExperience, additionalInfo);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CoverLetterDto.GenerateCompleteRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<CoverLetterDto.GenerateCompleteResponse> response = restTemplate.postForEntity(
                url, entity, CoverLetterDto.GenerateCompleteResponse.class
            );
            
            log.info("Cover letter generated for company: {}, position: {}", company, position);
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to generate cover letter: {}", e.getMessage());
            return createErrorCoverLetterResponse("자소서 생성 중 오류가 발생했습니다.");
        }
    }

    public CoverLetterDto.FeedbackResponse getCoverLetterFeedback(
            String coverLetterText, String position, String company) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/cover-letter/feedback";
            CoverLetterDto.FeedbackRequest request = 
                new CoverLetterDto.FeedbackRequest(coverLetterText, position, company);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CoverLetterDto.FeedbackRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<CoverLetterDto.FeedbackResponse> response = restTemplate.postForEntity(
                url, entity, CoverLetterDto.FeedbackResponse.class
            );
            
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to get cover letter feedback: {}", e.getMessage());
            return createErrorFeedbackResponse("자소서 피드백 중 오류가 발생했습니다.");
        }
    }

    // ============= Translation Services =============
    
    public TranslationDto.TranslateResponse translateText(
            String text, String targetLanguage, String sourceLanguage) {
        return translateText(text, targetLanguage, sourceLanguage, "general");
    }
    
    public TranslationDto.TranslateResponse translateText(
            String text, String targetLanguage, String sourceLanguage, String documentType) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/translation/translate";
            TranslationDto.TranslateRequest request = 
                new TranslationDto.TranslateRequest(text, targetLanguage, sourceLanguage, documentType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TranslationDto.TranslateRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TranslationDto.TranslateResponse> response = restTemplate.postForEntity(
                url, entity, TranslationDto.TranslateResponse.class
            );
            
            log.info("Text translated from {} to {} (document type: {})", sourceLanguage, targetLanguage, documentType);
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to translate text: {}", e.getMessage());
            return createErrorTranslationResponse("번역 중 오류가 발생했습니다.");
        }
    }

    // ============= Sentiment Analysis Services =============
    
    public SentimentAnalysisDto.AnalyzeResponse analyzeSentiment(String text, String language) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/sentiment/analyze";
            SentimentAnalysisDto.AnalyzeRequest request = 
                new SentimentAnalysisDto.AnalyzeRequest(text, language);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SentimentAnalysisDto.AnalyzeRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<SentimentAnalysisDto.AnalyzeResponse> response = restTemplate.postForEntity(
                url, entity, SentimentAnalysisDto.AnalyzeResponse.class
            );
            
            log.info("Sentiment analysis completed for text length: {}", text.length());
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to analyze sentiment: {}", e.getMessage());
            return createErrorSentimentResponse("감정 분석 중 오류가 발생했습니다.");
        }
    }

    // ============= Image Generation Services =============
    
    public ImageGenerationDto.GenerateResponse generateImage(
            String prompt, String style, String size, int n) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/image/generate";
            ImageGenerationDto.GenerateRequest request = 
                new ImageGenerationDto.GenerateRequest(prompt, style, size, n);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ImageGenerationDto.GenerateRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ImageGenerationDto.GenerateResponse> response = restTemplate.postForEntity(
                url, entity, ImageGenerationDto.GenerateResponse.class
            );
            
            log.info("Image generated with prompt: {}, style: {}", prompt, style);
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to generate image: {}", e.getMessage());
            return createErrorImageResponse("이미지 생성 중 오류가 발생했습니다.");
        }
    }

    // ============= PDF Generation Services =============
    
    public PdfGenerationDto.GeneratePdfResponse generateCoverLetterPdf(
            String applicantName, String position, String company, 
            String coverLetterContent, String contactInfo, String style) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/pdf/cover-letter";
            PdfGenerationDto.GenerateCoverLetterPdfRequest request = 
                new PdfGenerationDto.GenerateCoverLetterPdfRequest(
                    applicantName, position, company, coverLetterContent, contactInfo, style
                );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PdfGenerationDto.GenerateCoverLetterPdfRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PdfGenerationDto.GeneratePdfResponse> response = restTemplate.postForEntity(
                url, entity, PdfGenerationDto.GeneratePdfResponse.class
            );
            
            log.info("Cover letter PDF generated for: {}, position: {}", applicantName, position);
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to generate cover letter PDF: {}", e.getMessage());
            return createErrorPdfResponse("자소서 PDF 생성 중 오류가 발생했습니다.");
        }
    }
    
    public PdfGenerationDto.GeneratePdfResponse generateResumePdf(
            PdfGenerationDto.GenerateResumePdfRequest request) {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/pdf/resume";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PdfGenerationDto.GenerateResumePdfRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PdfGenerationDto.GeneratePdfResponse> response = restTemplate.postForEntity(
                url, entity, PdfGenerationDto.GeneratePdfResponse.class
            );
            
            log.info("Resume PDF generated for: {}", request.applicantName());
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to generate resume PDF: {}", e.getMessage());
            return createErrorPdfResponse("이력서 PDF 생성 중 오류가 발생했습니다.");
        }
    }

    // ============= Health Check =============
    
    public boolean checkAIServiceHealth() {
        try {
            String url = aiServiceConfig.getAiServiceBaseUrl() + "/api/v1/chatbot/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.contains("\"success\":true")) {
                    return true;
                }
            }
            
            log.warn("AI Service health check returned non-successful response: {}", response.getBody());
            return false;
        } catch (RestClientException e) {
            log.error("AI Service health check failed: {}", e.getMessage());
            return false;
        }
    }

    // ============= Error Response Creators =============
    
    private ChatbotDto.ChatResponse createErrorChatResponse(String errorMessage) {
        ChatbotDto.ChatData errorData = new ChatbotDto.ChatData(
            "죄송합니다. 현재 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", 
            "error", 0.0, 0.0
        );
        return new ChatbotDto.ChatResponse(false, errorMessage, errorData);
    }

    private InterviewDto.GenerateQuestionsResponse createErrorInterviewResponse(String errorMessage) {
        return new InterviewDto.GenerateQuestionsResponse(false, errorMessage, null);
    }

    private InterviewDto.EvaluateAnswerResponse createErrorEvaluationResponse(String errorMessage) {
        return new InterviewDto.EvaluateAnswerResponse(false, errorMessage, null);
    }

    private CoverLetterDto.GenerateCompleteResponse createErrorCoverLetterResponse(String errorMessage) {
        return new CoverLetterDto.GenerateCompleteResponse(false, errorMessage, null);
    }

    private CoverLetterDto.FeedbackResponse createErrorFeedbackResponse(String errorMessage) {
        return new CoverLetterDto.FeedbackResponse(false, errorMessage, null);
    }

    private TranslationDto.TranslateResponse createErrorTranslationResponse(String errorMessage) {
        return new TranslationDto.TranslateResponse(false, errorMessage, null);
    }

    private ImageGenerationDto.GenerateResponse createErrorImageResponse(String errorMessage) {
        return new ImageGenerationDto.GenerateResponse(false, errorMessage, null);
    }
    
    private SentimentAnalysisDto.AnalyzeResponse createErrorSentimentResponse(String errorMessage) {
        return new SentimentAnalysisDto.AnalyzeResponse(false, errorMessage, null);
    }
    
    private PdfGenerationDto.GeneratePdfResponse createErrorPdfResponse(String errorMessage) {
        return new PdfGenerationDto.GeneratePdfResponse(false, errorMessage, null);
    }
}