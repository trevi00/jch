package org.jbd.backend.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jbd.backend.ai.dto.CoverLetterDto;
import org.jbd.backend.ai.service.AICoverLetterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/cover-letter")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AICoverLetterController {

    private final AICoverLetterService aiCoverLetterService;

    @PostMapping("/generate")
    public ResponseEntity<CoverLetterDto.GenerateCompleteResponse> generateCoverLetter(
            @Valid @RequestBody GenerateCoverLetterRequest request) {
        
        CoverLetterDto.GenerateCompleteResponse response = aiCoverLetterService.generateCompleteCoverLetter(
            request.getCompany(),
            request.getPosition(),
            request.getUserExperience(),
            request.getAdditionalInfo()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<CoverLetterDto.FeedbackResponse> getCoverLetterFeedback(
            @Valid @RequestBody FeedbackRequest request) {
        
        CoverLetterDto.FeedbackResponse response = aiCoverLetterService.getCoverLetterFeedback(
            request.getCoverLetterText(),
            request.getPosition(),
            request.getCompany()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/guide")
    public ResponseEntity<CoverLetterGuideResponse> getCoverLetterGuide(
            @RequestParam(required = false) String position) {
        
        String guide = aiCoverLetterService.getCoverLetterGuide(position);
        CoverLetterGuideResponse response = new CoverLetterGuideResponse(guide);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/section-tips")
    public ResponseEntity<SectionTipsResponse> getSectionTips(
            @RequestParam String sectionType) {
        
        String tips = aiCoverLetterService.getSectionWritingTips(sectionType);
        SectionTipsResponse response = new SectionTipsResponse(sectionType, tips);
        
        return ResponseEntity.ok(response);
    }

    // Request DTOs
    public static class GenerateCoverLetterRequest {
        private String company;
        private String position;
        private String userExperience;
        private String additionalInfo;

        public GenerateCoverLetterRequest() {}

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getUserExperience() {
            return userExperience;
        }

        public void setUserExperience(String userExperience) {
            this.userExperience = userExperience;
        }

        public String getAdditionalInfo() {
            return additionalInfo;
        }

        public void setAdditionalInfo(String additionalInfo) {
            this.additionalInfo = additionalInfo;
        }
    }

    public static class FeedbackRequest {
        private String coverLetterText;
        private String position;
        private String company;

        public FeedbackRequest() {}

        public String getCoverLetterText() {
            return coverLetterText;
        }

        public void setCoverLetterText(String coverLetterText) {
            this.coverLetterText = coverLetterText;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }
    }

    // Response DTOs
    public static class CoverLetterGuideResponse {
        private String guide;

        public CoverLetterGuideResponse(String guide) {
            this.guide = guide;
        }

        public String getGuide() {
            return guide;
        }
    }

    public static class SectionTipsResponse {
        private String sectionType;
        private String tips;

        public SectionTipsResponse(String sectionType, String tips) {
            this.sectionType = sectionType;
            this.tips = tips;
        }

        public String getSectionType() {
            return sectionType;
        }

        public String getTips() {
            return tips;
        }
    }
}