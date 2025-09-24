package org.jbd.backend.auth.controller;

import jakarta.validation.Valid;
import org.jbd.backend.auth.dto.AuthenticationRequest;
import org.jbd.backend.auth.dto.AuthenticationResponse;
import org.jbd.backend.auth.service.AuthenticationService;
import org.jbd.backend.common.dto.ApiResponse;
import org.jbd.backend.user.dto.UserRegistrationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST API 컨트롤러
 *
 * 사용자의 회원가입, 로그인, 토큰 갱신, 로그아웃 등 인증과 관련된 API를 제공합니다.
 * JWT 토큰 기반 인증을 사용하며, 회원가입 시 자동으로 로그인 처리됩니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see AuthenticationService
 * @see AuthenticationRequest
 * @see AuthenticationResponse
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * AuthController 생성자
     *
     * @param authenticationService 인증 서비스
     */
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    /**
     * 새로운 사용자를 등록합니다.
     * 회원가입과 동시에 자동으로 로그인 처리되어 토큰을 반환합니다.
     *
     * @param request 회원가입 요청 데이터
     *                - email: 이메일 주소 (필수, 유일해야 함)
     *                - password: 비밀번호 (필수)
     *                - name: 사용자 이름 (필수)
     *                - userType: 사용자 타입 (GENERAL, COMPANY)
     * @return ResponseEntity<ApiResponse<AuthenticationResponse>> 회원가입 및 인증 결과
     * @apiNote POST /api/auth/register
     * @see UserRegistrationDto
     * @see AuthenticationResponse
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody UserRegistrationDto request
    ) {
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    /**
     * 사용자 로그인을 처리합니다.
     * 이메일과 비밀번호를 검증하고 JWT 토큰을 발급합니다.
     *
     * @param request 로그인 요청 데이터
     *                - email: 이메일 주소 (필수)
     *                - password: 비밀번호 (필수)
     * @return ResponseEntity<ApiResponse<AuthenticationResponse>> 로그인 결과 및 토큰
     * @apiNote POST /api/auth/login
     * @see AuthenticationRequest
     * @see AuthenticationResponse
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
    }

    /**
     * JWT 토큰을 갱신합니다.
     * Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
     *
     * @param authHeader Authorization 헤더 ("Bearer {refresh_token}" 형식)
     * @return ResponseEntity<ApiResponse<AuthenticationResponse>> 갱신된 토큰 정보
     * @apiNote POST /api/auth/refresh-token
     * @see AuthenticationResponse
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("유효하지 않은 토큰 형식입니다."));
        }

        String refreshToken = authHeader.substring(7);
        AuthenticationResponse response = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다.", response));
    }

    /**
     * 사용자 로그아웃을 처리합니다.
     * 토큰을 무효화하여 추가 사용을 방지합니다.
     *
     * @param authHeader Authorization 헤더 (선택사항, "Bearer {token}" 형식)
     * @return ResponseEntity<ApiResponse<Void>> 로그아웃 완료 응답
     * @apiNote POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authenticationService.logout(token);
        }

        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다."));
    }
}