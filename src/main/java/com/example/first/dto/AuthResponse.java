package com.example.first.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String token;
    private String username;
    private String message;
    private Long point;

    public AuthResponse(String token, String username, String message, Long point) {
        this.token = token;
        this.username = username;
        this.message = message;
        this.point = point;
    }
}
