package com.example.first.controller;

import com.example.first.dto.request.AuthRequest;
import com.example.first.dto.request.LogoutRequestDto;
import com.example.first.dto.response.AuthResponse;
import com.example.first.entity.AuthProvider;
import com.example.first.entity.User;
import com.example.first.service.AuthService;
import com.example.first.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        try {
            User newUser = authService.register(request);
            return new ResponseEntity<>("회원가입 성공: " + newUser.getUsername(), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        log.info("Logging in");
        try {
            AuthResponse res = authService.login(request);
            if(res != null) return new ResponseEntity<>(res, HttpStatus.OK);
            return new ResponseEntity<>("Not right user", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("NO", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequestDto dto) { //Always unauthorized when Authentication is required, why?
        try {
            if (dto == null) {
                return new ResponseEntity<>("올바르지 않은 유저 정보", HttpStatus.BAD_REQUEST);
            }
            String username = dto.getUsername();
            Optional<User> user = userService.findUserByUsername(username);
            if(user.isEmpty() || !dto.getToken().equals(user.get().getCurrentToken())){
                log.warn("유저 {}에게 잘못된 로그아웃 요청", dto.getUsername());
                return new ResponseEntity<>("허가되지 않은 로그아웃 요청", HttpStatus.UNAUTHORIZED);
            }
            authService.logout(username);
            log.info("로그아웃 성공: {}", username);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("로그아웃 실패", e);
            return new ResponseEntity<>("로그아웃 처리 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/login/google")
    public void googleLogin(HttpServletResponse response) throws Exception {
        response.sendRedirect(authService.getLoginDirection(AuthProvider.GOOGLE));
    }
    // GET /api/auth/login/kakao - 카카오 로그인
    @GetMapping("/login/kakao")
    public void kakaoLogin(HttpServletResponse response) throws Exception {
        response.sendRedirect(authService.getLoginDirection(AuthProvider.KAKAO));
    }

    @GetMapping("/login/naver")
    public void naverLogin(HttpServletResponse response) throws Exception {
        response.sendRedirect(authService.getLoginDirection(AuthProvider.NAVER));
    }
    // Handle OAuth
    @GetMapping("/oauth2/code/google")
    public void googleCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws Exception {
        AuthResponse auth = authService.handleGoogleCallback(code);
        String redirectUrl = "https://underminingly-semineutral-natacha.ngrok-free.dev/social"
                + "?token=" + auth.getToken()
                + "&username=" + auth.getUsername();

        response.sendRedirect(redirectUrl);
    }
    @GetMapping("/oauth2/code/kakao")
    public void kakaoCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws Exception {
        AuthResponse auth = authService.handleKakaoCallback(code);
        String redirectUrl = "https://underminingly-semineutral-natacha.ngrok-free.dev/social"
                + "?token=" + auth.getToken()
                + "&username=" + auth.getUsername();

        response.sendRedirect(redirectUrl);
    }
    @GetMapping("/oauth2/code/naver")
    public void naverCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response
    ) throws Exception {
        AuthResponse auth = authService.handleNaverCallback(code, state);
        String redirectUrl = "https://underminingly-semineutral-natacha.ngrok-free.dev/social"
                + "?token=" + auth.getToken()
                + "&username=" + auth.getUsername();

        response.sendRedirect(redirectUrl);
    }
}