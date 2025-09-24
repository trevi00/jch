package org.jbd.backend.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.ai.dto.CoverLetterDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AICoverLetterService {

    private final AIServiceClient aiServiceClient;

    /**
     * 완전한 자기소개서 생성
     */
    public CoverLetterDto.GenerateCompleteResponse generateCompleteCoverLetter(
            String company, String position, String userExperience, String additionalInfo) {
        
        log.info("Generating complete cover letter for company: {}, position: {}", company, position);
        
        // 입력값 유효성 검증
        if (company == null || company.trim().isEmpty()) {
            return createValidationErrorResponse("회사명을 입력해주세요.");
        }

        if (position == null || position.trim().isEmpty()) {
            return createValidationErrorResponse("지원 직무를 입력해주세요.");
        }

        if (userExperience == null || userExperience.trim().isEmpty()) {
            return createValidationErrorResponse("본인의 경험이나 역량을 입력해주세요.");
        }

        if (userExperience.length() < 20) {
            return createValidationErrorResponse("경험/역량 정보를 좀 더 자세히 입력해주세요. (최소 20자)");
        }

        if (userExperience.length() > 1000) {
            return createValidationErrorResponse("경험/역량 정보가 너무 깁니다. (최대 1000자)");
        }

        return aiServiceClient.generateCompleteCoverLetter(
            company.trim(), 
            position.trim(), 
            userExperience.trim(), 
            additionalInfo != null ? additionalInfo.trim() : ""
        );
    }

    /**
     * 자기소개서 피드백 제공
     */
    public CoverLetterDto.FeedbackResponse getCoverLetterFeedback(
            String coverLetterText, String position, String company) {
        
        log.info("Providing feedback for cover letter - company: {}, position: {}", company, position);
        
        // 입력값 유효성 검증
        if (coverLetterText == null || coverLetterText.trim().isEmpty()) {
            return createFeedbackValidationErrorResponse("피드백을 받을 자기소개서 내용을 입력해주세요.");
        }

        if (coverLetterText.length() < 100) {
            return createFeedbackValidationErrorResponse("자기소개서 내용이 너무 짧습니다. 최소 100자 이상 입력해주세요.");
        }

        if (coverLetterText.length() > 5000) {
            return createFeedbackValidationErrorResponse("자기소개서 내용이 너무 깁니다. 5000자 이하로 작성해주세요.");
        }

        if (position == null || position.trim().isEmpty()) {
            return createFeedbackValidationErrorResponse("지원 직무를 입력해주세요.");
        }

        if (company == null || company.trim().isEmpty()) {
            return createFeedbackValidationErrorResponse("지원 회사명을 입력해주세요.");
        }

        return aiServiceClient.getCoverLetterFeedback(
            coverLetterText.trim(), 
            position.trim(), 
            company.trim()
        );
    }

    /**
     * 자기소개서 작성 가이드 제공
     */
    public String getCoverLetterGuide(String position) {
        log.info("Getting cover letter guide for position: {}", position);
        
        return String.format("""
            %s 자기소개서 작성 가이드:
            
            1. 도입부 (지원 동기)
            - 회사와 직무에 대한 관심을 구체적으로 표현하세요
            - 회사의 가치나 비전과 본인의 목표를 연결지어 설명하세요
            
            2. 경험 및 역량
            - 지원 직무와 관련된 구체적인 경험을 STAR 기법으로 작성하세요
            - 성과는 가능한 정량적으로 표현하세요
            - 실패 경험도 포함하되, 배운 점과 개선 방안을 함께 서술하세요
            
            3. 성장 계획
            - 해당 직무에서의 단기/장기 목표를 제시하세요
            - 회사와 함께 성장하고자 하는 의지를 보여주세요
            
            4. 마무리
            - 간결하고 강력한 마무리 문장으로 인상을 남기세요
            - 면접에 대한 기대감을 표현하세요
            
            ✅ 작성 팁:
            - 한 문단당 3-4문장으로 구성하세요
            - 추상적인 표현보다는 구체적인 사례를 활용하세요
            - 회사 홈페이지, 뉴스 등을 참고해 회사에 대한 이해도를 높이세요
            - 맞춤법과 띄어쓰기를 꼼꼼히 확인하세요
            """, position != null ? position : "해당 직무");
    }

    /**
     * 자기소개서 섹션별 작성 팁
     */
    public String getSectionWritingTips(String sectionType) {
        return switch (sectionType.toLowerCase()) {
            case "introduction", "도입부" -> getIntroductionTips();
            case "experience", "경험" -> getExperienceTips();
            case "motivation", "동기" -> getMotivationTips();
            case "growth", "성장계획" -> getGrowthPlanTips();
            case "conclusion", "마무리" -> getConclusionTips();
            default -> "올바른 섹션 타입을 선택해주세요. (introduction, experience, motivation, growth, conclusion)";
        };
    }

    private String getIntroductionTips() {
        return """
            도입부 작성 팁:
            
            - 첫 인상이 중요합니다. 임팩트 있는 문장으로 시작하세요
            - 본인의 핵심 강점을 한 문장으로 요약해보세요
            - 지원 동기를 개인적인 경험과 연결지어 설명하세요
            - 일반적인 표현보다는 구체적이고 개성 있는 표현을 사용하세요
            """;
    }

    private String getExperienceTips() {
        return """
            경험 섹션 작성 팁:
            
            - STAR 기법을 활용하세요 (Situation, Task, Action, Result)
            - 정량적 성과를 포함하세요 (숫자, 퍼센트 등)
            - 지원 직무와 관련성이 높은 경험을 우선적으로 서술하세요
            - 팀워크, 리더십, 문제해결 능력을 보여주는 사례를 포함하세요
            """;
    }

    private String getMotivationTips() {
        return """
            지원 동기 작성 팁:
            
            - 회사에 대한 구체적인 정보를 포함하세요
            - 개인적인 가치관과 회사 가치의 일치점을 찾아 서술하세요
            - 단순한 관심 표현을 넘어서 구체적인 근거를 제시하세요
            - 회사에 기여할 수 있는 방안을 함께 언급하세요
            """;
    }

    private String getGrowthPlanTips() {
        return """
            성장 계획 작성 팁:
            
            - 단기(1년) 및 중장기(3-5년) 목표를 구분해서 작성하세요
            - 구체적이고 실현 가능한 계획을 제시하세요
            - 회사의 성장과 본인의 성장을 연결지어 설명하세요
            - 지속적인 학습 의지를 보여주세요
            """;
    }

    private String getConclusionTips() {
        return """
            마무리 섹션 작성 팁:
            
            - 앞서 언급한 내용을 간단히 요약하세요
            - 회사에 대한 열정과 의지를 재확인하세요
            - 면접 기회에 대한 기대감을 표현하세요
            - 강렬하고 기억에 남을 마무리 문장을 작성하세요
            """;
    }

    /**
     * 자기소개서 생성 유효성 검증 오류 응답
     */
    private CoverLetterDto.GenerateCompleteResponse createValidationErrorResponse(String message) {
        return new CoverLetterDto.GenerateCompleteResponse(false, message, null);
    }

    /**
     * 피드백 유효성 검증 오류 응답
     */
    private CoverLetterDto.FeedbackResponse createFeedbackValidationErrorResponse(String message) {
        return new CoverLetterDto.FeedbackResponse(false, message, null);
    }
}