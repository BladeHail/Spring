package com.example.first.service;

import com.example.first.dto.AuthRequest;
import com.example.first.entity.AuthProvider;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerSuccess() {
        // given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .provider(AuthProvider.LOCAL)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        User result = authService.register(authRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getProvider()).isEqualTo(AuthProvider.LOCAL);

        verify(userRepository).existsByUsername(anyString());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 사용자명")
    void registerFail_DuplicateUsername() {
        // given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(authRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 사용자 이름입니다.");

        verify(userRepository).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공 - JWT 토큰 발급")
    void loginSuccess() {
        // given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.createToken(authentication)).thenReturn("jwt-token-12345");

        // when
        String token = authService.login(authRequest);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isEqualTo("jwt-token-12345");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).createToken(authentication);
    }
}