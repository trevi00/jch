package org.jbd.backend.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 승급 요청 DTO
 *
 * 일반 사용자를 관리자로 승급하기 위한 요청 데이터를 담습니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-01-26
 */
@Data
@NoArgsConstructor
public class AdminPromoteRequest {

    /**
     * 승급할 사용자의 이메일 주소
     */
    private String email;

    /**
     * 관리자 승급을 위한 시크릿 키
     */
    private String secretKey;

    public AdminPromoteRequest(String email, String secretKey) {
        this.email = email;
        this.secretKey = secretKey;
    }
}