package org.jbd.backend.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 관리자 사용자 정보 DTO
 *
 * JWT 토큰에 포함되거나 응답으로 전송되는 관리자 사용자 정보를 담습니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-01-26
 */
@Data
@Builder
public class AdminUserInfo {

    /**
     * 사용자 ID
     */
    private Long id;

    /**
     * 사용자 이메일 주소
     */
    private String email;

    /**
     * 사용자 이름
     */
    private String name;

    /**
     * 사용자 타입 (ADMIN)
     */
    private String userType;

    /**
     * 관리자 여부 (항상 true)
     */
    private boolean isAdmin;

    /**
     * 사용자 역할 (ADMIN)
     */
    private String role;
}