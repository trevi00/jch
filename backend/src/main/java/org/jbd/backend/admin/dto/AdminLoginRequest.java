package org.jbd.backend.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 로그인 요청 DTO
 *
 * 관리자 로그인을 위한 자격증명 데이터를 담습니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-01-26
 */
@Data
@NoArgsConstructor
public class AdminLoginRequest {

    /**
     * 관리자 사용자의 이메일 주소
     */
    private String email;

    /**
     * 관리자 사용자의 비밀번호
     */
    private String password;

    public AdminLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}