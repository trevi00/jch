package org.jbd.backend.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.ai.dto.PdfGenerationDto;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.UserProfile;
import org.jbd.backend.user.repository.UserRepository;
import org.jbd.backend.user.repository.UserProfileRepository;
import org.jbd.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private final AIServiceClient aiServiceClient;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    /**
     * 자기소개서 PDF 생성
     */
    public PdfGenerationDto.GeneratePdfResponse generateCoverLetterPdf(
            String userEmail, String position, String company, 
            String coverLetterContent, String style) {
        
        log.info("Generating cover letter PDF for user: {}, position: {}, company: {}", 
                userEmail, position, company);
        
        User user = userRepository.findByEmailAndIsDeletedFalse(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);
        String contactInfo = buildContactInfo(user, userProfile);
        String userName = userProfile != null && userProfile.getFullName() != null ?
                         userProfile.getFullName() : "Unknown User";

        return aiServiceClient.generateCoverLetterPdf(
                userName, position, company, coverLetterContent, contactInfo, style
        );
    }
    
    /**
     * 이력서 PDF 생성 (사용자 프로필 기반)
     */
    public PdfGenerationDto.GeneratePdfResponse generateResumePdf(
            String userEmail, String style) {
        
        log.info("Generating resume PDF for user: {}", userEmail);
        
        User user = userRepository.findByEmailAndIsDeletedFalse(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);
        PdfGenerationDto.GenerateResumePdfRequest request = buildResumeRequest(user, userProfile, style);

        return aiServiceClient.generateResumePdf(request);
    }
    
    /**
     * 연락처 정보 구성
     */
    private String buildContactInfo(User user, UserProfile userProfile) {
        StringBuilder contactInfo = new StringBuilder();

        String fullName = userProfile != null && userProfile.getFullName() != null ?
                         userProfile.getFullName() : "Unknown User";
        contactInfo.append("이름: ").append(fullName).append("\n");
        contactInfo.append("이메일: ").append(user.getEmail()).append("\n");

        if (userProfile != null && userProfile.getPhoneNumber() != null) {
            contactInfo.append("전화번호: ").append(userProfile.getPhoneNumber()).append("\n");
        }

        if (userProfile != null && userProfile.getLocation() != null) {
            contactInfo.append("거주지역: ").append(userProfile.getLocation()).append("\n");
        }

        return contactInfo.toString();
    }
    
    /**
     * 이력서 요청 객체 구성
     */
    private PdfGenerationDto.GenerateResumePdfRequest buildResumeRequest(User user, UserProfile userProfile, String style) {
        String fullName = userProfile != null && userProfile.getFullName() != null ?
                         userProfile.getFullName() : "Unknown User";
        String phoneNumber = userProfile != null ? userProfile.getPhoneNumber() : null;
        String location = userProfile != null ? userProfile.getLocation() : null;
        String desiredJob = userProfile != null ? userProfile.getDesiredJob() : null;

        return new PdfGenerationDto.GenerateResumePdfRequest(
                fullName,
                user.getEmail(),
                phoneNumber,
                location, // 주소 대신 거주 지역 사용
                desiredJob, // objective 대신 희망 직무 사용
                List.of(), // 현재는 빈 리스트로 처리
                List.of(), // 현재는 빈 리스트로 처리
                List.of(), // 현재는 빈 리스트로 처리
                List.of(), // 현재는 빈 리스트로 처리
                style != null ? style : "modern"
        );
    }
    
}