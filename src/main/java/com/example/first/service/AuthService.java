package com.example.first.service;

import com.example.first.dto.AuthRequest;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // --- 회원가입 ---
    @Transactional
    public User register(AuthRequest request) {
        // 1. 아이디 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 사용자 객체 생성 및 저장
        User newUser = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .build();

        return userRepository.save(newUser);
    }

    // --- 로그인 ---
    @Transactional(readOnly = true)
    public String login(AuthRequest request) throws AuthenticationException {
        // 1. Spring Security의 AuthenticationManager를 사용하여 인증 시도
        // 이 과정에서 UserDetailsService가 비밀번호를 비교합니다.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. 인증 성공 시 JWT 토큰 생성 및 반환
        return jwtTokenProvider.createToken(authentication);
    }
}