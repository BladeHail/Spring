package com.example.first.dto;

import com.example.first.entity.AuthProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserResponseDto {
    private Long id;

    private String username;

    private String password;

    private String email;

    private boolean isAdmin = false;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;
}
