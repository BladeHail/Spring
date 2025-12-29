package com.example.first.dto.response;

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

    private String email;

    private boolean isAdmin = false;

    private Long point;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
}
