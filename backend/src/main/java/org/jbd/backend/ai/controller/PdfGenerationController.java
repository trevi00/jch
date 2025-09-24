package org.jbd.backend.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbd.backend.ai.dto.PdfGenerationDto;
import org.jbd.backend.ai.service.PdfGenerationService;
import org.jbd.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PdfGenerationController {

    private final PdfGenerationService pdfGenerationService;

    /**
     * 자기소개서 PDF 생성
     */
    @PostMapping("/cover-letter")
    public ResponseEntity<ApiResponse<PdfGenerationDto.PdfData>> generateCoverLetterPdf(
            @Valid @RequestBody CoverLetterPdfRequest request,
            Authentication authentication) {
        
        log.info("Cover letter PDF generation requested by: {}", authentication.getName());
        
        try {
            PdfGenerationDto.GeneratePdfResponse response = pdfGenerationService.generateCoverLetterPdf(
                    authentication.getName(),
                    request.position(),
                    request.company(),
                    request.coverLetterContent(),
                    request.style()
            );
            
            if (response.success()) {
                return ResponseEntity.ok(
                        ApiResponse.success("자기소개서 PDF가 성공적으로 생성되었습니다.", response.data())
                );
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(response.message()));
            }
            
        } catch (Exception e) {
            log.error("Cover letter PDF generation failed for user: {}, error: {}", 
                    authentication.getName(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("자기소개서 PDF 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 이력서 PDF 생성
     */
    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<PdfGenerationDto.PdfData>> generateResumePdf(
            @Valid @RequestBody ResumePdfRequest request,
            Authentication authentication) {
        
        log.info("Resume PDF generation requested by: {}", authentication.getName());
        
        try {
            PdfGenerationDto.GeneratePdfResponse response = pdfGenerationService.generateResumePdf(
                    authentication.getName(),
                    request.style()
            );
            
            if (response.success()) {
                return ResponseEntity.ok(
                        ApiResponse.success("이력서 PDF가 성공적으로 생성되었습니다.", response.data())
                );
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(response.message()));
            }
            
        } catch (Exception e) {
            log.error("Resume PDF generation failed for user: {}, error: {}", 
                    authentication.getName(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("이력서 PDF 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * PDF 다운로드 (URL 기반)
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<ApiResponse<String>> getDownloadUrl(
            @PathVariable String fileName,
            Authentication authentication) {
        
        log.info("PDF download requested by: {} for file: {}", authentication.getName(), fileName);
        
        try {
            // 실제로는 파일 저장소에서 다운로드 URL을 생성해야 함
            // 현재는 임시로 간단한 URL 반환
            String downloadUrl = "/api/pdf/files/" + fileName;
            
            return ResponseEntity.ok(
                    ApiResponse.success("다운로드 URL이 생성되었습니다.", downloadUrl)
            );
            
        } catch (Exception e) {
            log.error("PDF download URL generation failed for user: {}, file: {}, error: {}", 
                    authentication.getName(), fileName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("다운로드 URL 생성 중 오류가 발생했습니다."));
        }
    }

    // DTO Records for Requests
    public record CoverLetterPdfRequest(
            String position,
            String company,
            String coverLetterContent,
            String style
    ) {}
    
    public record ResumePdfRequest(
            String style
    ) {}
}