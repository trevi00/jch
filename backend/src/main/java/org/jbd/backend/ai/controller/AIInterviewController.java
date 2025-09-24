package org.jbd.backend.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jbd.backend.ai.domain.Interview;
import org.jbd.backend.ai.dto.InterviewDto;
import org.jbd.backend.ai.dto.InterviewDto.GenerateQuestionsRequest;
import org.jbd.backend.ai.service.AIInterviewService;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/interview")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIInterviewController {

    private final AIInterviewService aiInterviewService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/questions")
    public ResponseEntity<InterviewDto.GenerateQuestionsResponse> generateQuestions(
            @Valid @RequestBody GenerateQuestionsRequest request) {
        
        InterviewDto.GenerateQuestionsResponse response = aiInterviewService.generateInterviewQuestions(
            request.getPosition(),
            request.getInterviewType(),
            request.getCount()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<InterviewDto.EvaluateAnswerResponse> evaluateAnswer(
            @Valid @RequestBody EvaluateAnswerRequest request) {
        
        InterviewDto.EvaluateAnswerResponse response = aiInterviewService.evaluateInterviewAnswer(
            request.getQuestion(),
            request.getAnswer(),
            request.getPosition()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete")
    public ResponseEntity<InterviewDto.CompleteInterviewResponse> completeInterview(
            @Valid @RequestBody InterviewDto.CompleteInterviewRequest request,
            @RequestHeader("Authorization") String authorization) {

        try {
            // JWT 토큰에서 사용자 정보 추출
            String token = authorization.replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(token);
            User user = userService.findUserByEmail(userEmail);

            // 면접 완료 처리
            InterviewDto.CompleteInterviewResponse response = aiInterviewService.completeInterview(
                user.getId(), request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new InterviewDto.CompleteInterviewResponse(
                    false,
                    "면접 완료 처리 중 오류가 발생했습니다: " + e.getMessage(),
                    null
                )
            );
        }
    }

    @GetMapping("/guide")
    public ResponseEntity<InterviewGuideResponse> getInterviewGuide(
            @RequestParam String position,
            @RequestParam String interviewType) {
        
        String guide = aiInterviewService.getInterviewGuide(position, interviewType);
        InterviewGuideResponse response = new InterviewGuideResponse(guide);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history")
    public ResponseEntity<Page<AIInterviewService.InterviewHistoryDto>> getInterviewHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authorization) {

        try {
            // JWT 토큰에서 사용자 정보 추출
            String token = authorization.replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(token);
            User user = userService.findUserByEmail(userEmail);

            Pageable pageable = PageRequest.of(page, size);
            Page<AIInterviewService.InterviewHistoryDto> interviewHistory = aiInterviewService.getUserInterviewHistory(
                user.getId(), pageable);

            return ResponseEntity.ok(interviewHistory);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<AIInterviewService.InterviewStats> getInterviewStats(
            @RequestHeader("Authorization") String authorization) {
        
        try {
            // JWT 토큰에서 사용자 정보 추출
            String token = authorization.replace("Bearer ", "");
            String userEmail = jwtService.extractUsername(token);
            User user = userService.findUserByEmail(userEmail);
            
            AIInterviewService.InterviewStats stats = aiInterviewService.getUserInterviewStats(user.getId());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Request DTOs
    public static class GenerateQuestionsRequest {
        private String position;
        private String interviewType;
        private int count = 5; // default value

        public GenerateQuestionsRequest() {}

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getInterviewType() {
            return interviewType;
        }

        public void setInterviewType(String interviewType) {
            this.interviewType = interviewType;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class EvaluateAnswerRequest {
        private String question;
        private String answer;
        private String position;

        public EvaluateAnswerRequest() {}

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }


    public static class InterviewGuideResponse {
        private String guide;

        public InterviewGuideResponse(String guide) {
            this.guide = guide;
        }

        public String getGuide() {
            return guide;
        }
    }
}