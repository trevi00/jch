package org.jbd.backend.ai.service;

import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.ai.dto.ChatbotDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AI 챗봇 서비스 테스트")
class AIChatbotServiceTest {

    @Mock
    private AIServiceClient aiServiceClient;

    @InjectMocks
    private AIChatbotService aiChatbotService;

    private ChatbotDto.ChatResponse successResponse;
    private ChatbotDto.ChatData chatData;

    @BeforeEach
    void setUp() {
        chatData = new ChatbotDto.ChatData(
            "안녕하세요! 어떻게 도와드릴까요?",
            "greeting",
            0.95,
            0.5
        );
        
        successResponse = new ChatbotDto.ChatResponse(true, "응답 성공", chatData);
    }

    @Test
    @DisplayName("정상적인 채팅 메시지 처리")
    void 정상적인_채팅_메시지_처리() {
        // given
        String userId = "test@example.com";
        String message = "안녕하세요";
        
        given(aiServiceClient.chatWithBot(userId, message)).willReturn(successResponse);

        // when
        ChatbotDto.ChatResponse result = aiChatbotService.processChat(userId, message);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getResponse()).contains("안녕하세요");
        verify(aiServiceClient).chatWithBot(userId, message);
    }

    @Test
    @DisplayName("빈 메시지 입력 시 유효성 검증 오류")
    void 빈_메시지_입력_시_유효성_검증_오류() {
        // given
        String userId = "test@example.com";
        String emptyMessage = "";

        // when
        ChatbotDto.ChatResponse result = aiChatbotService.processChat(userId, emptyMessage);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("메시지를 입력해주세요");
        assertThat(result.getData().getCategory()).isEqualTo("validation_error");
    }

    @Test
    @DisplayName("너무 긴 메시지 입력 시 유효성 검증 오류")
    void 너무_긴_메시지_입력_시_유효성_검증_오류() {
        // given
        String userId = "test@example.com";
        String longMessage = "a".repeat(1001); // 1001자

        // when
        ChatbotDto.ChatResponse result = aiChatbotService.processChat(userId, longMessage);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("1000자 이하로");
        assertThat(result.getData().getCategory()).isEqualTo("validation_error");
    }

    @Test
    @DisplayName("유효한 카테고리로 빠른 응답 조회")
    void 유효한_카테고리로_빠른_응답_조회() {
        // given
        String userId = "test@example.com";
        String category = "회원가입";
        
        given(aiServiceClient.getQuickResponse(userId, category)).willReturn(successResponse);

        // when
        ChatbotDto.ChatResponse result = aiChatbotService.getQuickResponse(userId, category);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        verify(aiServiceClient).getQuickResponse(userId, category);
    }

    @Test
    @DisplayName("유효하지 않은 카테고리로 빠른 응답 조회 시 오류")
    void 유효하지_않은_카테고리로_빠른_응답_조회_시_오류() {
        // given
        String userId = "test@example.com";
        String invalidCategory = "invalid_category";

        // when
        ChatbotDto.ChatResponse result = aiChatbotService.getQuickResponse(userId, invalidCategory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("지원하지 않는 카테고리");
        assertThat(result.getData().getCategory()).isEqualTo("validation_error");
    }

    @Test
    @DisplayName("AI 서비스 상태 확인")
    void AI_서비스_상태_확인() {
        // given
        given(aiServiceClient.checkAIServiceHealth()).willReturn(true);

        // when
        boolean result = aiChatbotService.isAIServiceAvailable();

        // then
        assertThat(result).isTrue();
        verify(aiServiceClient).checkAIServiceHealth();
    }

    @Test
    @DisplayName("채팅 히스토리 삭제")
    void 채팅_히스토리_삭제() {
        // given
        String userId = "test@example.com";

        // when & then (예외 발생하지 않음을 확인)
        aiChatbotService.clearChatHistory(userId);
        
        // 현재는 로깅만 수행하므로 예외가 발생하지 않으면 성공
    }
}