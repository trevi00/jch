package org.jbd.backend.payment.dto;

import lombok.Data;

@Data
public class KakaoPayReadyResponseDto {
    private String tid;
    private String next_redirect_pc_url;
    private String next_redirect_mobile_url;
    private String android_app_scheme;
    private String ios_app_scheme;
    private String created_at;
}