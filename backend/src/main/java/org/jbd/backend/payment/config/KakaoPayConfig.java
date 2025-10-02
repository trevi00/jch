package org.jbd.backend.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 카카오페이 API 설정
 */
@Configuration
@ConfigurationProperties(prefix = "kakaopay.api")
@Getter
@Setter
public class KakaoPayConfig {

    private String baseUrl;
    private String adminKey;
    private String cid;
    private String successUrl;
    private String cancelUrl;
    private String failUrl;

    public String getAuthorizationHeader() {
        return "KakaoAK " + adminKey;
    }
}