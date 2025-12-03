package com.example.first.entity;

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
    private String password;

    @Column
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    private String providerId;

    private String profileImage;

    @Column(nullable = false)
    private Long tokenVersion = 0L;

    @Builder
    public User(String username, String password, String email, AuthProvider provider, String providerId, String profileImage) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
        this.profileImage = profileImage;
        this.tokenVersion = 0L;
    }
    public void updateOAuthInfo(String username, String profileImage) {
        this.username = username;
        this.profileImage = profileImage;
    }
    public void logout() {
        this.tokenVersion++;
    }
    public void invalidateOAuthToken() {
        this.tokenVersion++;
    }
}
