package com.example.first.service;

import com.example.first.dto.AuthRequest;
import com.example.first.entity.AuthProvider;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${oauth.naver.client-id}")
    private String naverClientId;

    @Value("${oauth.naver.redirect-uri}")
    private String naverRedirectUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;


    @Transactional
    public User register(AuthRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(null)
                .provider(AuthProvider.LOCAL)
                .providerId(null)
                .profileImage(null)
                .build();

        return userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public String login(AuthRequest request) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        return jwtTokenProvider.createToken(authentication);
    }

    @Transactional(readOnly = true)
    public String getLoginDirection(AuthProvider type) {
        String url;
        switch(type) {
            case GOOGLE:
                url = "https://accounts.google.com/o/oauth2/v2/auth?";
                url += "client_id=" + googleClientId;
                url += "&redirect_uri=" + googleRedirectUri;
                break;
            case NAVER:
                url = "Naver";
                break;
            case KAKAO:
                url = "Kakao";
                break;
            default:
                url = "";
                break;
        }
        url += "&response_type=code";
        url += "&scope=email";
        return url;
    }
}