package org.jbd.backend.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.ai.domain.Interview;
import org.jbd.backend.ai.domain.InterviewQuestion;
import org.jbd.backend.ai.domain.InterviewStatus;
import org.jbd.backend.ai.dto.InterviewDto;
import org.jbd.backend.ai.repository.InterviewRepository;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInterviewService {

    private final AIServiceClient aiServiceClient;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;

    /**
     * 직무별 면접 질문 생성
     */
    public InterviewDto.GenerateQuestionsResponse generateInterviewQuestions(
            String position, String interviewType, int count) {
        
        log.info("Generating {} interview questions for position: {}, type: {}", count, position, interviewType);
        
        // 입력값 유효성 검증
        if (position == null || position.trim().isEmpty()) {
            return createValidationErrorResponse("직무를 입력해주세요.");
        }

        if (!isValidInterviewType(interviewType)) {
            return createValidationErrorResponse("올바른 면접 유형을 선택해주세요. (technical, behavioral, general)");
        }

        if (count < 1 || count > 20) {
            return createValidationErrorResponse("질문 개수는 1개에서 20개 사이로 설정해주세요.");
        }

        return aiServiceClient.generateInterviewQuestions(position.trim(), interviewType, count);
    }

    /**
     * 면접 답변 평가
     */
    public InterviewDto.EvaluateAnswerResponse evaluateInterviewAnswer(
            String question, String answer, String position) {
        
        log.info("Evaluating interview answer for position: {}", position);
        
        // 입력값 유효성 검증
        if (question == null || question.trim().isEmpty()) {
            return createEvaluationValidationErrorResponse("면접 질문이 필요합니다.");
        }

        if (answer == null || answer.trim().isEmpty()) {
            return createEvaluationValidationErrorResponse("답변을 입력해주세요.");
        }

        if (answer.length() < 10) {
            return createEvaluationValidationErrorResponse("답변이 너무 짧습니다. 좀 더 자세히 작성해주세요.");
        }

        if (answer.length() > 2000) {
            return createEvaluationValidationErrorResponse("답변이 너무 깁니다. 2000자 이하로 작성해주세요.");
        }

        if (position == null || position.trim().isEmpty()) {
            return createEvaluationValidationErrorResponse("직무 정보가 필요합니다.");
        }

        return aiServiceClient.evaluateAnswer(question.trim(), answer.trim(), position.trim());
    }

    /**
     * 면접 세션 완료 처리
     */
    public void completeInterviewSession(String sessionId, int totalQuestions, int answeredQuestions) {
        log.info("Completing interview session: {} ({}/{} questions answered)", 
                sessionId, answeredQuestions, totalQuestions);
        
        // 면접 완료 통계 처리 로직
        double completionRate = (double) answeredQuestions / totalQuestions * 100;
        log.info("Interview completion rate: {}%", String.format("%.1f", completionRate));
        
        // 실제로는 사용자의 면접 이력에 저장하거나 통계를 업데이트하는 로직이 필요
    }

    /**
     * 직무별 추천 면접 준비 가이드 제공
     */
    public String getInterviewGuide(String position, String interviewType) {
        log.info("Getting interview guide for position: {}, type: {}", position, interviewType);
        
        return switch (interviewType.toLowerCase()) {
            case "technical" -> getTechnicalInterviewGuide(position);
            case "behavioral" -> getBehavioralInterviewGuide();
            case "general" -> getGeneralInterviewGuide();
            default -> "올바른 면접 유형을 선택해주세요.";
        };
    }

    /**
     * 면접 유형 유효성 검증
     */
    private boolean isValidInterviewType(String interviewType) {
        if (interviewType == null) return false;
        
        Set<String> validTypes = Set.of("technical", "behavioral", "general");
        return validTypes.contains(interviewType.toLowerCase());
    }

    /**
     * 기술 면접 가이드
     */
    private String getTechnicalInterviewGuide(String position) {
        return String.format("""
            %s 기술 면접 준비 가이드:
            
            1. 기본 개념 복습
            - 해당 분야의 핵심 개념들을 정리하세요
            - 실무에서 사용했던 기술 스택을 설명할 수 있도록 준비하세요
            
            2. 코딩 테스트 준비
            - 알고리즘과 자료구조 기본기를 점검하세요
            - 문제 해결 과정을 논리적으로 설명하는 연습을 하세요
            
            3. 프로젝트 경험 정리
            - 주요 프로젝트의 기술적 도전과 해결 방법을 준비하세요
            - 성능 최적화나 문제 해결 경험을 구체적으로 설명할 수 있도록 하세요
            """, position);
    }

    /**
     * 행동 면접 가이드
     */
    private String getBehavioralInterviewGuide() {
        return """
            행동 면접 준비 가이드:
            
            1. STAR 기법 활용
            - Situation(상황), Task(과제), Action(행동), Result(결과)로 답변을 구성하세요
            
            2. 주요 경험 정리
            - 팀워크, 리더십, 문제해결, 갈등해결 경험을 준비하세요
            - 실패 경험과 그로부터 배운 점을 솔직하게 준비하세요
            
            3. 회사/직무 관련 동기
            - 지원 동기와 장기적 커리어 계획을 명확히 하세요
            """;
    }

    /**
     * 일반 면접 가이드
     */
    private String getGeneralInterviewGuide() {
        return """
            일반 면접 준비 가이드:
            
            1. 자기소개 준비
            - 1분, 3분, 5분 버전으로 준비하세요
            - 핵심 강점과 경험을 중심으로 구성하세요
            
            2. 회사 연구
            - 회사의 비전, 문화, 최근 동향을 파악하세요
            - 지원 부서의 역할과 중요성을 이해하세요
            
            3. 질문 준비
            - 회사와 직무에 대한 궁금한 점을 미리 준비하세요
            - 성장 가능성과 관련된 질문을 준비하세요
            """;
    }

    /**
     * 질문 생성 유효성 검증 오류 응답
     */
    private InterviewDto.GenerateQuestionsResponse createValidationErrorResponse(String message) {
        return new InterviewDto.GenerateQuestionsResponse(false, message, null);
    }

    /**
     * 면접 완료 처리 및 데이터베이스 저장
     */
    @Transactional
    public InterviewDto.CompleteInterviewResponse completeInterview(
            Long userId, InterviewDto.CompleteInterviewRequest request) {
        
        log.info("Completing interview for user: {}, jobRole: {}", userId, request.getJobRole());
        
        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            // 면접 엔티티 생성
            Interview interview = new Interview(
                user, 
                request.getJobRole(), 
                "technical", // 기본값, 나중에 프론트엔드에서 전달받도록 개선 가능
                "junior" // 기본값, 나중에 프론트엔드에서 전달받도록 개선 가능
            );
            
            // 질문과 답변 처리
            double totalScore = 0;
            int scoredAnswers = 0;
            
            for (int i = 0; i < request.getQuestions().size(); i++) {
                InterviewDto.QuestionData questionData = request.getQuestions().get(i);
                
                // 해당 질문에 대한 답변 찾기
                InterviewDto.AnswerData answerData = request.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(questionData.getId()))
                    .findFirst()
                    .orElse(null);
                
                // 면접 질문 엔티티 생성
                InterviewQuestion question = new InterviewQuestion(
                    interview,
                    questionData.getId(),
                    questionData.getQuestion(),
                    questionData.getType()
                );
                question.setQuestionOrder(i + 1);
                
                // 답변이 있는 경우 설정
                if (answerData != null) {
                    question.setAnswer(answerData.getAnswer());
                    
                    // 피드백이 있는 경우 설정
                    if (answerData.getFeedback() != null) {
                        InterviewDto.FeedbackData feedback = answerData.getFeedback();
                        question.setFeedback(
                            feedback.getScore() != null ? BigDecimal.valueOf(feedback.getScore()) : null,
                            feedback.getStrengths(),
                            feedback.getImprovements(),
                            feedback.getSuggestion()
                        );
                        
                        if (feedback.getScore() != null) {
                            totalScore += feedback.getScore();
                            scoredAnswers++;
                        }
                    }
                }
                
                interview.addQuestion(question);
            }
            
            // 전체 점수 계산
            BigDecimal overallScore = scoredAnswers > 0 ? 
                BigDecimal.valueOf(totalScore / scoredAnswers) : BigDecimal.ZERO;
            
            // 면접 완료 처리
            interview.complete(overallScore, null); // 시간은 나중에 프론트엔드에서 전달받도록 개선 가능
            
            // 데이터베이스 저장
            Interview savedInterview = interviewRepository.save(interview);
            
            // 응답 생성
            InterviewDto.InterviewResultData resultData = new InterviewDto.InterviewResultData(
                savedInterview.getId(),
                overallScore.doubleValue(),
                savedInterview.getTotalQuestions(),
                savedInterview.getAnsweredQuestions(),
                savedInterview.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            return new InterviewDto.CompleteInterviewResponse(
                true,
                "면접이 성공적으로 완료되었습니다.",
                resultData
            );
            
        } catch (Exception e) {
            log.error("Error completing interview for user: {}", userId, e);
            return new InterviewDto.CompleteInterviewResponse(
                false,
                "면접 완료 처리 중 오류가 발생했습니다: " + e.getMessage(),
                null
            );
        }
    }
    
    /**
     * 사용자의 면접 이력 조회
     */
    @Transactional(readOnly = true)
    public Page<InterviewHistoryDto> getUserInterviewHistory(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Interview> interviews = interviewRepository.findByUserWithQuestionsAndIsDeletedFalse(user, pageable);

        return interviews.map(interview -> new InterviewHistoryDto(
            interview.getId(),
            interview.getJobRole(),
            interview.getInterviewType(),
            interview.getExperienceLevel(),
            interview.getOverallScore(),
            interview.getTotalQuestions(),
            interview.getAnsweredQuestions(),
            interview.getStatus().name(),
            interview.getCompletedAt(),
            interview.getCreatedAt(),
            interview.getQuestions().size()
        ));
    }

    /**
     * 면접 히스토리 DTO 클래스
     */
    public static class InterviewHistoryDto {
        private final Long id;
        private final String jobRole;
        private final String interviewType;
        private final String experienceLevel;
        private final java.math.BigDecimal overallScore;
        private final Integer totalQuestions;
        private final Integer answeredQuestions;
        private final String status;
        private final java.time.LocalDateTime completedAt;
        private final java.time.LocalDateTime createdAt;
        private final Integer questionCount;

        public InterviewHistoryDto(Long id, String jobRole, String interviewType, String experienceLevel,
                                  java.math.BigDecimal overallScore, Integer totalQuestions, Integer answeredQuestions,
                                  String status, java.time.LocalDateTime completedAt, java.time.LocalDateTime createdAt,
                                  Integer questionCount) {
            this.id = id;
            this.jobRole = jobRole;
            this.interviewType = interviewType;
            this.experienceLevel = experienceLevel;
            this.overallScore = overallScore;
            this.totalQuestions = totalQuestions;
            this.answeredQuestions = answeredQuestions;
            this.status = status;
            this.completedAt = completedAt;
            this.createdAt = createdAt;
            this.questionCount = questionCount;
        }

        // Getters
        public Long getId() { return id; }
        public String getJobRole() { return jobRole; }
        public String getInterviewType() { return interviewType; }
        public String getExperienceLevel() { return experienceLevel; }
        public java.math.BigDecimal getOverallScore() { return overallScore; }
        public Integer getTotalQuestions() { return totalQuestions; }
        public Integer getAnsweredQuestions() { return answeredQuestions; }
        public String getStatus() { return status; }
        public java.time.LocalDateTime getCompletedAt() { return completedAt; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public Integer getQuestionCount() { return questionCount; }
    }
    
    /**
     * 사용자의 면접 통계 조회
     */
    @Transactional(readOnly = true)
    public InterviewStats getUserInterviewStats(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        long totalInterviews = interviewRepository.countByUserAndStatusAndIsDeletedFalse(
            user, InterviewStatus.COMPLETED);
        
        Double averageScore = interviewRepository.findAverageScoreByUserAndStatus(
            user, InterviewStatus.COMPLETED);
        
        return new InterviewStats(totalInterviews, averageScore != null ? averageScore : 0.0);
    }
    
    /**
     * 면접 통계 데이터 클래스
     */
    public static class InterviewStats {
        private final long totalInterviews;
        private final double averageScore;
        
        public InterviewStats(long totalInterviews, double averageScore) {
            this.totalInterviews = totalInterviews;
            this.averageScore = averageScore;
        }
        
        public long getTotalInterviews() {
            return totalInterviews;
        }
        
        public double getAverageScore() {
            return averageScore;
        }
    }

    /**
     * 답변 평가 유효성 검증 오류 응답
     */
    private InterviewDto.EvaluateAnswerResponse createEvaluationValidationErrorResponse(String message) {
        return new InterviewDto.EvaluateAnswerResponse(false, message, null);
    }
}