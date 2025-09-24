package org.jbd.backend.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jbd.backend.ai.dto.ChatbotDto;
import org.jbd.backend.ai.service.AIChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 챗봇 REST API 컨트롤러
 *
 * 사용자와 AI 간의 채팅 기능, 빠른 응답 기능, 채팅 히스토리 관리 등
 * AI 챗봇과 관련된 모든 기능을 제공합니다. 외부 AI 서비스와 연동하여
 * 지능형 대화 서비스를 구현합니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see AIChatbotService
 * @see ChatbotDto
 */
@RestController
@RequestMapping("/ai/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIChatbotController {

    /** AI 챗봇 비즈니스 로직을 처리하는 서비스 */
    private final AIChatbotService aiChatbotService;

    /**
     * AI와 채팅을 수행합니다.
     * 사용자의 메시지를 받아 AI 서비스에 전달하고 응답을 반환합니다.
     * 채팅 기록은 사용자별로 관리되며 컨텍스트가 유지됩니다.
     *
     * @param request 채팅 요청 데이터
     *                - message: 사용자 메시지 (필수)
     * @param userEmail 사용자 이메일 (헤더를 통해 전달)
     * @return ResponseEntity<ChatbotDto.ChatResponse> AI 응답 데이터
     * @apiNote POST /ai/chatbot/chat
     * @see ChatRequest
     * @see ChatbotDto.ChatResponse
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatbotDto.ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("X-User-Email") String userEmail) {
        
        ChatbotDto.ChatResponse response = aiChatbotService.processChat(userEmail, request.getMessage());
        return ResponseEntity.ok(response);
    }

    /**
     * 빠른 응답을 제공합니다.
     * 미리 정의된 카테고리에 따라 즉석에서 적절한 AI 응답을 생성합니다.
     * 자주 사용되는 질문에 대한 빠른 답변을 제공합니다.
     *
     * @param request 빠른 응답 요청 데이터
     *                - category: 응답 카테고리 (필수)
     * @param userEmail 사용자 이메일 (헤더를 통해 전달)
     * @return ResponseEntity<ChatbotDto.ChatResponse> AI 빠른 응답 데이터
     * @apiNote POST /ai/chatbot/quick-response
     * @see QuickResponseRequest
     * @see ChatbotDto.ChatResponse
     */
    @PostMapping("/quick-response")
    public ResponseEntity<ChatbotDto.ChatResponse> getQuickResponse(
            @Valid @RequestBody QuickResponseRequest request,
            @RequestHeader("X-User-Email") String userEmail) {
        
        ChatbotDto.ChatResponse response = aiChatbotService.getQuickResponse(userEmail, request.getCategory());
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 채팅 히스토리를 삭제합니다.
     * 해당 사용자의 모든 채팅 기록과 컨텍스트가 영구적으로 삭제됩니다.
     *
     * @param userEmail 사용자 이메일 (헤더를 통해 전달)
     * @return ResponseEntity<String> 삭제 완료 메시지
     * @apiNote DELETE /ai/chatbot/history
     */
    @DeleteMapping("/history")
    public ResponseEntity<String> clearChatHistory(@RequestHeader("X-User-Email") String userEmail) {
        aiChatbotService.clearChatHistory(userEmail);
        return ResponseEntity.ok("채팅 히스토리가 삭제되었습니다.");
    }

    /**
     * AI 챗봇 서비스의 상태를 확인합니다.
     * 외부 AI 서비스와의 연결 상태를 점검하고 서비스 가용성을 보고합니다.
     *
     * @return ResponseEntity<HealthResponse> 서비스 상태 정보
     * @apiNote GET /ai/chatbot/health
     * @see HealthResponse
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> checkHealth() {
        boolean isHealthy = aiChatbotService.isAIServiceAvailable();
        HealthResponse response = new HealthResponse(isHealthy, 
            isHealthy ? "AI 챗봇 서비스가 정상 작동 중입니다." : "AI 챗봇 서비스에 문제가 있습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅 요청 DTO 클래스
     *
     * AI 챗봇과의 채팅을 위한 사용자 메시지를 담는 클래스입니다.
     *
     * @author JBD Backend Team
     * @version 1.0
     * @since 2025-09-19
     */
    public static class ChatRequest {
        /** 사용자가 전송하는 메시지 */
        private String message;

        /** 기본 생성자 */
        public ChatRequest() {}

        /**
         * 메시지를 반환합니다.
         * @return 사용자 메시지
         */
        public String getMessage() {
            return message;
        }

        /**
         * 메시지를 설정합니다.
         * @param message 설정할 메시지
         */
        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 빠른 응답 요청 DTO 클래스
     *
     * 미리 정의된 카테고리에 따른 빠른 AI 응답을 요청하기 위한 클래스입니다.
     *
     * @author JBD Backend Team
     * @version 1.0
     * @since 2025-09-19
     */
    public static class QuickResponseRequest {
        /** 빠른 응답 카테고리 (예: FAQ, 도움말, 안내 등) */
        private String category;

        /** 기본 생성자 */
        public QuickResponseRequest() {}

        /**
         * 카테고리를 반환합니다.
         * @return 빠른 응답 카테고리
         */
        public String getCategory() {
            return category;
        }

        /**
         * 카테고리를 설정합니다.
         * @param category 설정할 카테고리
         */
        public void setCategory(String category) {
            this.category = category;
        }
    }

    /**
     * 헬스 체크 응답 DTO 클래스
     *
     * AI 챗봇 서비스의 상태 정보를 담는 클래스입니다.
     * 서비스 가용성과 상태 메시지를 제공합니다.
     *
     * @author JBD Backend Team
     * @version 1.0
     * @since 2025-09-19
     */
    public static class HealthResponse {
        /** 서비스 정상 작동 여부 */
        private boolean healthy;
        /** 서비스 상태 메시지 */
        private String message;

        /**
         * HealthResponse 생성자
         *
         * @param healthy 서비스 정상 작동 여부
         * @param message 서비스 상태 메시지
         */
        public HealthResponse(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
        }

        /**
         * 서비스 정상 작동 여부를 반환합니다.
         * @return 서비스 정상 작동 여부
         */
        public boolean isHealthy() {
            return healthy;
        }

        /**
         * 서비스 상태 메시지를 반환합니다.
         * @return 서비스 상태 메시지
         */
        public String getMessage() {
            return message;
        }
    }
}