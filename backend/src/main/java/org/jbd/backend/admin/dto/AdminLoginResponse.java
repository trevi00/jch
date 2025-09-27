package org.jbd.backend.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 관리자 로그인 응답 DTO
 *
 * 관리자 로그인 성공 시 반환되는 토큰과 사용자 정보를 담습니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-01-26
 */
@Data
@Builder
public class AdminLoginResponse {

    /**
     * 관리자 사용자 정보
     */
    private AdminUserInfo user;

    /**
     * JWT 액세스 토큰
     */
    private String accessToken;

    /**
     * JWT 리프레시 토큰
     */
    private String refreshToken;

    /**
     * 토큰 타입 (일반적으로 "Bearer")
     */
    private String tokenType;

    /**
     * 토큰 만료 시간 (초 단위)
     */
    private long expiresIn;
}