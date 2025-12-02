package com.example.first.service;

import com.example.first.dto.AuthRequest;
import com.example.first.dto.AuthResponse;
import com.example.first.entity.AuthProvider;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.security.jwt.JwtTokenProvider;
import com.example.first.security.oauth2.GoogleUserInfo;
import com.example.first.dto.GoogleTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.math.BigInteger;
import java.security.SecureRandom;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

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
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .provider(AuthProvider.LOCAL)
                .build();

        return userRepository.save(newUser);
    }

    public String login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            String token = jwtTokenProvider.createToken(
                    user.getUsername(),
                    user.getTokenVersion()
            );
            log.info("로그인 성공: username={}, tokenVersion={}",
                    user.getUsername(),
                    user.getTokenVersion());
            return token;
        } catch (AuthenticationException e) {
            log.warn("로그인 실패: {}", request.getUsername());
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.logout();
        userRepository.save(user);

        log.info("로그아웃 성공: username={}, newTokenVersion={}",
                username, user.getTokenVersion());
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
                String state = new BigInteger(130, new SecureRandom()).toString();

                url = "https://nid.naver.com/oauth2.0/authorize?";
                url += "client_id=" + naverClientId;
                url += "&redirect_uri=" + naverRedirectUri;
                url += "&response_type=code";
                url += "&state=" + state;
                break;
            case KAKAO:
                url = "https://kauth.kakao.com/oauth/authorize?";
                url += "client_id=" + kakaoClientId;
                url += "&redirect_uri=" + kakaoRedirectUri;
                break;
            default:
                url = "";
                break;
        }
        url += "&response_type=code";
        url += "&scope=email";
        return url;
    }

    //Handle OAuth
    @Transactional
    public AuthResponse handleGoogleCallback(String code) {

        // 1. Google Token 서버로 code 교환
        GoogleTokenResponse tokenResponse = getGoogleTokens(code);

        // 2. access_token으로 userinfo 가져오기
        GoogleUserInfo googleUser = getGoogleUserInfo(tokenResponse.getAccessToken());
        System.out.println(getGoogleUserInfo(tokenResponse.getAccessToken()).getAttributes());

        // 3. local DB에서 회원 조회/생성
        User user = userRepository.findByEmail(googleUser.getEmail())
                .orElseGet(() -> createGoogleUser(googleUser));

        // 4. JWT 발급
        String jwt = jwtTokenProvider.createToken(user.getUsername(), user.getTokenVersion());

        return new AuthResponse(jwt, user.getUsername(), "OAuth 로그인 성공");
    }

    private GoogleTokenResponse getGoogleTokens(String code) {

        RestTemplate rest = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        ResponseEntity<GoogleTokenResponse> response = rest.postForEntity(
                "https://oauth2.googleapis.com/token",
                request,
                GoogleTokenResponse.class
        );

        return response.getBody();
    }

    public GoogleUserInfo getGoogleUserInfo(String accessToken) {

        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = rest.exchange(
                "https://openidconnect.googleapis.com/v1/userinfo",
                HttpMethod.GET,
                request,
                Map.class
        );

        return new GoogleUserInfo(response.getBody());
    }
    // 누락된 메서드 추가
    private User createGoogleUser(GoogleUserInfo googleUser) {
        User newUser = User.builder()
                .username(googleUser.getEmail()) // 이메일을 username으로 사용
                .email(googleUser.getEmail())
                .password(passwordEncoder.encode((UUID.randomUUID().toString() + UUID.randomUUID().toString())))
                .provider(AuthProvider.GOOGLE)
                .providerId(googleUser.getSub()) // Google의 고유 ID
                .build();
                    //password is null
        User savedUser = userRepository.save(newUser);
        log.info("새로운 Google 사용자 생성: email={}, providerId={}",
                googleUser.getEmail(), googleUser.getSub());

        return savedUser;
    }

}