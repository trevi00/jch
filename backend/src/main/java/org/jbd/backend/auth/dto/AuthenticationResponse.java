package org.jbd.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jbd.backend.user.dto.UserResponseDto;

public class AuthenticationResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    private UserResponseDto user;
    
    public AuthenticationResponse() {}
    
    public AuthenticationResponse(String accessToken, String refreshToken, UserResponseDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.expiresIn = 86400L; // 24 hours in seconds
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private UserResponseDto user;
        
        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }
        
        public Builder user(UserResponseDto user) {
            this.user = user;
            return this;
        }
        
        public AuthenticationResponse build() {
            return new AuthenticationResponse(accessToken, refreshToken, user);
        }
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public UserResponseDto getUser() {
        return user;
    }
    
    public void setUser(UserResponseDto user) {
        this.user = user;
    }
}