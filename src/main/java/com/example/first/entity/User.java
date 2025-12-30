package com.example.first.entity;

import com.example.first.dto.response.UserResponseDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private String display;

    @Column
    private String password;

    @Column
    private String email;

    @Column(nullable = false)
    private boolean isAdmin = false;

    @Column(nullable = false)
    private Long point;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    private String providerId;

    private String profileImage;

    @Column(nullable = false)
    private Long tokenVersion = 0L;

    @Column
    private String currentToken;

    @Builder
    public User(String username, String password, String email, AuthProvider provider, String providerId, String profileImage, Long point) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.display = (email == null) ? username : email;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
        this.profileImage = profileImage;
        this.tokenVersion = 0L;
        this.point = point;
    }
    public void updateOAuthInfo(String username, String profileImage) {
        this.username = username;
        this.profileImage = profileImage;
    }
    public void updateDisplayName(String displayName) {
        this.display = displayName;
    }
    public void updatePoint(Long point) {
        this.point = point;
    }
    public void logout() {
        this.currentToken = null;
    }
    public void newToken(String token) {
        this.currentToken = token;
    }
    public void updateTokenVersion() {
        this.tokenVersion++;
    }
    public UserResponseDto toDto() {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(id);
        dto.setAdmin(isAdmin);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setProvider(provider);
        dto.setPoint(point);
        return dto;
    }
}
