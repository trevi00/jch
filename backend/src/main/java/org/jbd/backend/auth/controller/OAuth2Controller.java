package org.jbd.backend.auth.controller;

import org.jbd.backend.auth.dto.AuthenticationResponse;
import org.jbd.backend.auth.dto.OAuth2AuthenticationRequest;
import org.jbd.backend.auth.dto.OAuth2LoginRequest;
import org.jbd.backend.auth.service.OAuth2Service;
import org.jbd.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/auth/oauth2")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:3003"})
public class OAuth2Controller {
    
    private final OAuth2Service oauth2Service;
    
    public OAuth2Controller(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
    }
    
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticateWithGoogle(
            @Valid @RequestBody OAuth2LoginRequest request
    ) {
        AuthenticationResponse response = oauth2Service.authenticateWithGoogle(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Google 로그인 성공", response)
        );
    }
    
    @GetMapping("/google/url")
    public ResponseEntity<ApiResponse<String>> getGoogleAuthUrl(
            @RequestParam(required = false, defaultValue = "http://localhost:3000/auth/callback") String redirectUri,
            @RequestParam(required = false, defaultValue = "GENERAL") String userType,
            @RequestParam(required = false, defaultValue = "SIGNUP") String action
    ) {
        // UUID, userType, action을 조합한 state 생성
        String state = UUID.randomUUID().toString() + "|" + userType + "|" + action;
        String authUrl = oauth2Service.getGoogleAuthorizationUrl(redirectUri, state);
        
        return ResponseEntity.ok(
            ApiResponse.success("Google 인증 URL 생성 성공", authUrl)
        );
    }
    
    @PostMapping("/authorize")
    public ResponseEntity<ApiResponse<String>> getAuthorizationUrl(
            @Valid @RequestBody OAuth2AuthenticationRequest request
    ) {
        if (!"google".equals(request.getProvider())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("지원하지 않는 OAuth2 제공자입니다: " + request.getProvider())
            );
        }
        
        String state = request.getState() != null ? request.getState() : UUID.randomUUID().toString();
        String redirectUri = request.getRedirectUri() != null ? 
            request.getRedirectUri() : "http://localhost:3000/auth/callback";
            
        String authUrl = oauth2Service.getGoogleAuthorizationUrl(redirectUri, state);
        
        return ResponseEntity.ok(
            ApiResponse.success("인증 URL 생성 성공", authUrl)
        );
    }
    
    @PostMapping("/google/callback")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> handleGoogleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "redirectUri", required = false, defaultValue = "http://localhost:3000/auth/callback") String redirectUri
    ) {
        try {
            AuthenticationResponse response = oauth2Service.handleGoogleCallback(code, state, redirectUri);
            
            return ResponseEntity.ok(
                ApiResponse.success("Google OAuth2 콜백 처리 성공", response)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("OAuth2 콜백 처리 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }
    
    @GetMapping("/google/callback")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> handleGoogleCallbackRedirect(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "redirectUri", required = false, defaultValue = "http://localhost:3000/auth/callback") String redirectUri
    ) {
        try {
            AuthenticationResponse response = oauth2Service.handleGoogleCallback(code, state, redirectUri);
            
            return ResponseEntity.ok(
                ApiResponse.success("Google OAuth2 콜백 처리 성공", response)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("OAuth2 콜백 처리 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }
}