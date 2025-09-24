package org.jbd.backend.ai.service;

import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.ai.dto.InterviewDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AI 면접 서비스 테스트")
class AIInterviewServiceTest {

    @Mock
    private AIServiceClient aiServiceClient;

    @InjectMocks
    private AIInterviewService aiInterviewService;

    private InterviewDto.GenerateQuestionsResponse successQuestionsResponse;
    private InterviewDto.EvaluateAnswerResponse successEvaluationResponse;

    @BeforeEach
    void setUp() {
        // Mock 면접 질문 응답 설정
        List<InterviewDto.Question> questions = Arrays.asList(
            new InterviewDto.Question(
                "자기소개를 해주세요",
                "general",
                "basic",
                Arrays.asList("경험", "강점", "목표")
            ),
            new InterviewDto.Question(
                "우리 회사에 지원한 이유는 무엇인가요?",
                "motivation",
                "basic",
                Arrays.asList("회사", "비전", "성장")
            )
        );

        InterviewDto.QuestionsData questionsData = new InterviewDto.QuestionsData(
            questions, "백엔드 개발자", "general", 1.2
        );

        successQuestionsResponse = new InterviewDto.GenerateQuestionsResponse(
            true, "질문 생성 성공", questionsData
        );

        // Mock 답변 평가 응답 설정
        InterviewDto.EvaluationData evaluationData = new InterviewDto.EvaluationData(
            85,
            "좋은 답변입니다. 구체적인 경험을 잘 설명하셨네요.",
            Arrays.asList("구체적 사례 제시", "논리적 구성"),
            Arrays.asList("감정 표현 개선", "더 간결한 설명"),
            0.8
        );

        successEvaluationResponse = new InterviewDto.EvaluateAnswerResponse(
            true, "평가 완료", evaluationData
        );
    }

    @Test
    @DisplayName("정상적인 면접 질문 생성")
    void 정상적인_면접_질문_생성() {
        // given
        String position = "백엔드 개발자";
        String interviewType = "technical";
        int count = 5;
        
        given(aiServiceClient.generateInterviewQuestions(position, interviewType, count))
            .willReturn(successQuestionsResponse);

        // when
        InterviewDto.GenerateQuestionsResponse result = 
            aiInterviewService.generateInterviewQuestions(position, interviewType, count);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getQuestions()).hasSize(2);
        verify(aiServiceClient).generateInterviewQuestions(position, interviewType, count);
    }

    @Test
    @DisplayName("빈 직무명으로 질문 생성 시 유효성 검증 오류")
    void 빈_직무명으로_질문_생성_시_유효성_검증_오류() {
        // given
        String emptyPosition = "";
        String interviewType = "technical";
        int count = 5;

        // when
        InterviewDto.GenerateQuestionsResponse result = 
            aiInterviewService.generateInterviewQuestions(emptyPosition, interviewType, count);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("직무를 입력해주세요");
    }

    @Test
    @DisplayName("유효하지 않은 면접 유형으로 질문 생성 시 오류")
    void 유효하지_않은_면접_유형으로_질문_생성_시_오류() {
        // given
        String position = "백엔드 개발자";
        String invalidInterviewType = "invalid_type";
        int count = 5;

        // when
        InterviewDto.GenerateQuestionsResponse result = 
            aiInterviewService.generateInterviewQuestions(position, invalidInterviewType, count);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("올바른 면접 유형을 선택해주세요");
    }

    @Test
    @DisplayName("질문 개수 범위 초과 시 유효성 검증 오류")
    void 질문_개수_범위_초과_시_유효성_검증_오류() {
        // given
        String position = "백엔드 개발자";
        String interviewType = "technical";
        int invalidCount = 25; // 20개 초과

        // when
        InterviewDto.GenerateQuestionsResponse result = 
            aiInterviewService.generateInterviewQuestions(position, interviewType, invalidCount);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("1개에서 20개 사이로");
    }

    @Test
    @DisplayName("정상적인 답변 평가")
    void 정상적인_답변_평가() {
        // given
        String question = "자기소개를 해주세요";
        String answer = "안녕하세요. 3년간 백엔드 개발을 담당한 개발자입니다. 주로 Spring Boot와 MySQL을 사용하여...";
        String position = "백엔드 개발자";
        
        given(aiServiceClient.evaluateAnswer(question, answer, position))
            .willReturn(successEvaluationResponse);

        // when
        InterviewDto.EvaluateAnswerResponse result = 
            aiInterviewService.evaluateInterviewAnswer(question, answer, position);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getScore()).isEqualTo(85);
        assertThat(result.getData().getFeedback()).contains("좋은 답변");
        verify(aiServiceClient).evaluateAnswer(question, answer, position);
    }

    @Test
    @DisplayName("너무 짧은 답변 평가 시 유효성 검증 오류")
    void 너무_짧은_답변_평가_시_유효성_검증_오류() {
        // given
        String question = "자기소개를 해주세요";
        String shortAnswer = "개발자"; // 10자 미만
        String position = "백엔드 개발자";

        // when
        InterviewDto.EvaluateAnswerResponse result = 
            aiInterviewService.evaluateInterviewAnswer(question, shortAnswer, position);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("답변이 너무 짧습니다");
    }

    @Test
    @DisplayName("면접 가이드 조회 - 기술 면접")
    void 면접_가이드_조회_기술면접() {
        // given
        String position = "백엔드 개발자";
        String interviewType = "technical";

        // when
        String guide = aiInterviewService.getInterviewGuide(position, interviewType);

        // then
        assertThat(guide).isNotNull();
        assertThat(guide).contains("기술 면접 준비 가이드");
        assertThat(guide).contains("기본 개념 복습");
        assertThat(guide).contains("코딩 테스트 준비");
    }

    @Test
    @DisplayName("면접 가이드 조회 - 행동 면접")
    void 면접_가이드_조회_행동면접() {
        // given
        String position = "프론트엔드 개발자";
        String interviewType = "behavioral";

        // when
        String guide = aiInterviewService.getInterviewGuide(position, interviewType);

        // then
        assertThat(guide).isNotNull();
        assertThat(guide).contains("행동 면접 준비 가이드");
        assertThat(guide).contains("STAR 기법");
        assertThat(guide).contains("팀워크");
    }

    @Test
    @DisplayName("면접 세션 완료 처리")
    void 면접_세션_완료_처리() {
        // given
        String sessionId = "session_123";
        int totalQuestions = 10;
        int answeredQuestions = 8;

        // when & then (예외 발생하지 않음을 확인)
        aiInterviewService.completeInterviewSession(sessionId, totalQuestions, answeredQuestions);
        
        // 현재는 로깅만 수행하므로 예외가 발생하지 않으면 성공
    }
}