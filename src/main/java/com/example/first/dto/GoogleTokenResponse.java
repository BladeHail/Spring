package com.example.first.dto;

import lombok.Data;

@Data
public class GoogleTokenResponse {
    private String access_token;
    private String id_token;
    private String token_type;
    private Integer expires_in; // ← Integer로 선언
    private String scope;

    public String getAccessToken() {
        return access_token;
    }
}
