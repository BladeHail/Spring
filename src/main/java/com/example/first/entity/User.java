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

    @Column(nullable = false)
    private String password; // 인코딩된 비밀번호

    // 역할(Role) 등 추가 정보는 필요에 따라 추가합니다.

    @Builder
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
