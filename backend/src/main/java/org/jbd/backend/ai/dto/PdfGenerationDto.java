package org.jbd.backend.ai.dto;

import java.util.List;

public class PdfGenerationDto {
    
    public record GenerateCoverLetterPdfRequest(
            String applicantName,
            String position,
            String company,
            String coverLetterContent,
            String contactInfo,
            String style  // "modern", "classic", "minimal"
    ) {}
    
    public record GeneratePdfResponse(
            boolean success,
            String message,
            PdfData data
    ) {}
    
    public record PdfData(
            String fileName,
            String downloadUrl,
            String base64Content,
            long fileSize,
            String mimeType
    ) {}
    
    public record CoverLetterSection(
            String title,
            String content
    ) {}
    
    public record GenerateResumePdfRequest(
            String applicantName,
            String email,
            String phone,
            String address,
            String objective,
            List<EducationEntry> education,
            List<ExperienceEntry> experience,
            List<SkillEntry> skills,
            List<CertificationEntry> certifications,
            String style
    ) {}
    
    public record EducationEntry(
            String institution,
            String degree,
            String major,
            String period,
            String gpa
    ) {}
    
    public record ExperienceEntry(
            String company,
            String position,
            String period,
            String description
    ) {}
    
    public record SkillEntry(
            String category,
            String name,
            String level
    ) {}
    
    public record CertificationEntry(
            String name,
            String issuer,
            String date,
            String expirationDate
    ) {}
}