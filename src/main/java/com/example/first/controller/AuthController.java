package com.example.first.controller;

import com.example.first.dto.AuthRequest;
import com.example.first.dto.AuthResponse;
import com.example.first.dto.OAthClientInfo;
import com.example.first.entity.User;
import com.example.first.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //소셜로그인  엔드포인트 google
    @GetMapping("/login/google")
    public RedirectView redirectToGoogle() {
        return new RedirectView("/oauth2/authorization/google");
    }
    //kakao 로그인 엔드포인트

    @GetMapping("/login/kakao")
    public RedirectView redirectToKakaoLogin() {
        return new RedirectView("/oauth2/authorization/kakao");
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        try {
            User newUser = authService.register(request);
            return new ResponseEntity<>("회원가입 성공: " + newUser.getUsername(), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            String token = authService.login(request);

            AuthResponse response = new AuthResponse(
                    token,
                    request.getUsername(),
                    "로그인 성공 및 토큰 발급"
            );

            return ResponseEntity.ok(response); // 200 OK
        } catch (Exception e) {
            // 인증 실패 시 (비밀번호 불일치, 사용자 없음 등)
            return new ResponseEntity<>(
                    new AuthResponse(null, request.getUsername(), "로그인 실패: 사용자 이름 또는 비밀번호 불일치"),
                    HttpStatus.UNAUTHORIZED // 401 Unauthorized
            );
        }
    }
    @GetMapping("/client-info")
    public List<OAthClientInfo> getClientInfo(){
        List<OAthClientInfo> infoList = new ArrayList<>();
        infoList.add(new  OAthClientInfo(
                "google",
                "254999034916-61o7vuis0demhdt8jrb1210d92r8o8nn.apps.googleusercontent.com",
                "http://localhost:8080/api/auth/register"
        ));
        infoList.add(new  OAthClientInfo(
                "kakao",
                "454f615976d86f74a3fcaabb05dca4d0",
                "http://localhost:8080/api/auth/register"
        ));
        return infoList;
    }
}