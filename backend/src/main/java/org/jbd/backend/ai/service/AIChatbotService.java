package org.jbd.backend.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.ai.dto.ChatbotDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatbotService {

    private final AIServiceClient aiServiceClient;

    /**
     * 사용자와 AI 챗봇 간의 대화 처리
     */
    public ChatbotDto.ChatResponse processChat(String userId, String message) {
        log.info("Processing chat for user: {} with message: {}", userId, message.substring(0, Math.min(50, message.length())));
        
        // 메시지 유효성 검증
        if (message == null || message.trim().isEmpty()) {
            return createValidationErrorResponse("메시지를 입력해주세요.");
        }

        if (message.length() > 1000) {
            return createValidationErrorResponse("메시지는 1000자 이하로 입력해주세요.");
        }

        return aiServiceClient.chatWithBot(userId, message.trim());
    }

    /**
     * 카테고리별 빠른 응답 제공
     */
    public ChatbotDto.ChatResponse getQuickResponse(String userId, String category) {
        log.info("Getting quick response for user: {} in category: {}", userId, category);
        
        // 카테고리 유효성 검증
        if (!isValidCategory(category)) {
            return createValidationErrorResponse("지원하지 않는 카테고리입니다.");
        }

        return aiServiceClient.getQuickResponse(userId, category);
    }

    /**
     * 사용자별 채팅 히스토리 초기화 (로그아웃 시 등)
     */
    public void clearChatHistory(String userId) {
        log.info("Chat history cleared for user: {}", userId);
        // 실제로는 AI 서비스의 히스토리 삭제 API를 호출해야 함
        // 현재는 로깅만 수행
    }

    /**
     * AI 서비스 상태 확인
     */
    public boolean isAIServiceAvailable() {
        return aiServiceClient.checkAIServiceHealth();
    }

    /**
     * 유효한 카테고리인지 확인
     */
    private boolean isValidCategory(String category) {
        if (category == null) return false;
        
        return switch (category.toLowerCase()) {
            case "회원가입", "로그인", "비밀번호", "프로필", "계정관리",
                 "자소서", "이력서", "포트폴리오", "취업준비",
                 "면접", "면접준비", "모의면접", "면접질문",
                 "구인구직", "취업정보", "채용공고", "기업정보",
                 "일반문의", "기술지원", "서비스문의" -> true;
            default -> false;
        };
    }

    /**
     * 유효성 검증 오류 응답 생성
     */
    private ChatbotDto.ChatResponse createValidationErrorResponse(String message) {
        ChatbotDto.ChatData errorData = new ChatbotDto.ChatData(
            message, "validation_error", 0.0, 0.0
        );
        return new ChatbotDto.ChatResponse(false, message, errorData);
    }
}