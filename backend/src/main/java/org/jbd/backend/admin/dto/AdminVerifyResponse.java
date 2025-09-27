package org.jbd.backend.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 관리자 권한 검증 응답 DTO
 *
 * JWT 토큰을 통한 관리자 권한 검증 결과를 담습니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-01-26
 */
@Data
@Builder
public class AdminVerifyResponse {

    /**
     * 관리자 권한 여부
     */
    private boolean isAdmin;

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 사용자 이메일 주소
     */
    private String email;

    /**
     * 사용자 이름
     */
    private String name;

    /**
     * 사용자 역할 (ADMIN)
     */
    private String role;
}