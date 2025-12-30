package com.example.first.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String username;
    private String password;
    private String email;
}