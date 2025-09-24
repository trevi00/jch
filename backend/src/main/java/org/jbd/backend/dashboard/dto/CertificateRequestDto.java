package org.jbd.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jbd.backend.dashboard.domain.CertificateRequest;
import org.jbd.backend.dashboard.domain.CertificateType;
import org.jbd.backend.dashboard.domain.enums.RequestStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CertificateRequestDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDto {
        @NotNull(message = "증명서 유형은 필수입니다")
        private CertificateType certificateType;

        @NotBlank(message = "사용 목적은 필수입니다")
        private String purpose;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDto {
        private Long id;
        private String userEmail;
        private String userName;
        private CertificateType certificateType;
        private String certificateTypeDescription;
        private RequestStatus status;
        private String statusDescription;
        private String purpose;
        private String adminNotes;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;

        public static ResponseDto from(CertificateRequest request) {
            return ResponseDto.builder()
                    .id(request.getId())
                    .userEmail(request.getUser().getEmail())
                    .userName(extractUserName(request.getUser().getEmail()))
                    .certificateType(request.getCertificateType())
                    .certificateTypeDescription(request.getCertificateType().getDescription())
                    .status(request.getStatus())
                    .statusDescription(request.getStatus().getDescription())
                    .purpose(request.getPurpose())
                    .adminNotes(request.getAdminNotes())
                    .createdAt(request.getCreatedAt())
                    .processedAt(request.getProcessedAt())
                    .build();
        }

        /**
         * 이메일에서 사용자 이름을 안전하게 추출하는 헬퍼 메서드
         * @param email 사용자 이메일
         * @return 이메일의 로컬 부분 또는 기본값
         */
        private static String extractUserName(String email) {
            if (email != null && email.contains("@")) {
                return email.split("@")[0];
            }
            return "Unknown User";
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessDto {
        @NotNull(message = "승인/거부 여부는 필수입니다")
        private Boolean approved;

        private String adminNotes;
    }
}