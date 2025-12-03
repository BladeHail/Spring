package com.example.first.controller;

import com.example.first.dto.AuthRequest;
import com.example.first.entity.AuthProvider;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 성공")
    void registerSuccess() throws Exception {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("회원가입 성공")));

        // 검증
        User savedUser = userRepository.findByUsername("newuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getProvider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 실패 (중복 사용자명)")
    void registerFail_DuplicateUsername() throws Exception {
        // given
        User existingUser = User.builder()
                .username("existinguser")
                .password(passwordEncoder.encode("password"))
                .provider(AuthProvider.LOCAL)
                .build();
        userRepository.save(existingUser);

        AuthRequest request = new AuthRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("이미 존재하는 사용자")));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 성공")
    void loginSuccess() throws Exception {
        // given
        User user = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .provider(AuthProvider.LOCAL)
                .build();
        userRepository.save(user);

        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.message").value("로그인 성공 및 토큰 발급"));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 실패 (잘못된 비밀번호)")
    void loginFail_WrongPassword() throws Exception {
        // given
        User user = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("correctpassword"))
                .provider(AuthProvider.LOCAL)
                .build();
        userRepository.save(user);

        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("로그인 실패")));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 실패 (존재하지 않는 사용자)")
    void loginFail_UserNotFound() throws Exception {
        // given
        AuthRequest request = new AuthRequest();
        request.setUsername("nonexistentuser");
        request.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("OAuth2 클라이언트 정보 조회 API 테스트")
    void getClientInfo() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/client-info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].provider").exists())
                .andExpect(jsonPath("$[0].clientId").exists());
    }
}