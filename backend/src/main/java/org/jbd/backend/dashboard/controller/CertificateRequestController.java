package org.jbd.backend.dashboard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jbd.backend.auth.service.JwtService;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.common.dto.PageResponse;
import org.jbd.backend.dashboard.dto.CertificateRequestDto;
import org.jbd.backend.dashboard.service.CertificateRequestService;
import org.jbd.backend.user.domain.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateRequestController {

    private final CertificateRequestService certificateRequestService;
    private final JwtService jwtService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<CertificateRequestDto.ResponseDto>> requestCertificate(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CertificateRequestDto.CreateDto dto) {
        try {
            Long userId = jwtService.extractUserId(token.substring(7));
            CertificateRequestDto.ResponseDto response = certificateRequestService.requestCertificate(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("증명서 요청이 등록되었습니다", response));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<CertificateRequestDto.ResponseDto>>> getMyRequests(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtService.extractUserId(token.substring(7));
            List<CertificateRequestDto.ResponseDto> requests = certificateRequestService.getUserRequests(userId);
            return ResponseEntity.ok(ApiResponse.success("내 증명서 요청 목록 조회 성공", requests));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<PageResponse<CertificateRequestDto.ResponseDto>>> getAllRequests(
            @RequestHeader("Authorization") String token,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            // 어드민 권한 검증 (AdminController와 동일한 방식)
            String jwt = token.replace("Bearer ", "");
            String userTypeStr = jwtService.extractUserType(jwt);

            if (!"ADMIN".equals(userTypeStr)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("관리자만 접근할 수 있습니다"));
            }

            Page<CertificateRequestDto.ResponseDto> requests = certificateRequestService.getAllRequests(pageable);
            return ResponseEntity.ok(ApiResponse.success("전체 증명서 요청 목록 조회 성공", new PageResponse<>(requests)));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/admin/{requestId}/process")
    public ResponseEntity<ApiResponse<CertificateRequestDto.ResponseDto>> processRequest(
            @RequestHeader("Authorization") String token,
            @PathVariable Long requestId,
            @Valid @RequestBody CertificateRequestDto.ProcessDto dto) {
        try {
            // 어드민 권한 검증 (AdminController와 동일한 방식)
            String jwt = token.replace("Bearer ", "");
            String userTypeStr = jwtService.extractUserType(jwt);

            if (!"ADMIN".equals(userTypeStr)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("관리자만 접근할 수 있습니다"));
            }

            CertificateRequestDto.ResponseDto response = certificateRequestService.processRequest(requestId, dto);
            String message = dto.getApproved() ? "증명서 요청이 승인되었습니다" : "증명서 요청이 거부되었습니다";
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/admin/{requestId}/complete")
    public ResponseEntity<ApiResponse<CertificateRequestDto.ResponseDto>> completeRequest(
            @RequestHeader("Authorization") String token,
            @PathVariable Long requestId) {
        try {
            // 어드민 권한 검증 (AdminController와 동일한 방식)
            String jwt = token.replace("Bearer ", "");
            String userTypeStr = jwtService.extractUserType(jwt);

            if (!"ADMIN".equals(userTypeStr)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("관리자만 접근할 수 있습니다"));
            }

            CertificateRequestDto.ResponseDto response = certificateRequestService.completeRequest(requestId);
            return ResponseEntity.ok(ApiResponse.success("증명서 발급이 완료되었습니다", response));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증에 실패했습니다: " + e.getMessage()));
        }
    }
}