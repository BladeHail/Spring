package com.example.first.controller;

import com.example.first.dto.AuthRequest;
import com.example.first.dto.AuthResponse;
import com.example.first.dto.OAthClientInfo;
import com.example.first.entity.AuthProvider;
import com.example.first.entity.User;
import com.example.first.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


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

    // GET /api/auth/login/google - 구글 로그인
    @GetMapping("/login/google")
    public void googleLogin(HttpServletResponse response) throws Exception {
        response.sendRedirect("/oauth2/authorization/google");
    }
    @GetMapping("/login/google/test")
    public String googleTestLogin(HttpServletResponse response) throws Exception {
        return authService.getLoginDirection(AuthProvider.GOOGLE);
    }
    // GET /api/auth/login/kakao - 카카오 로그인
    @GetMapping("/login/kakao")
    public void kakaoLogin(HttpServletResponse response) throws Exception {
        response.sendRedirect("/oauth2/authorization/kakao");
    }

    // GET /api/auth/login/naver - 네이버 로그인 (새로 추가!)
    @GetMapping("/login/naver")
    public void naverLogin(HttpServletResponse response) throws Exception {
        response.sendRedirect("/oauth2/authorization/naver");
    }

    // OAuth2 클라이언트 정보 반환
    @GetMapping("/client-info")
    public List<OAthClientInfo> getClientInfo(){
        List<OAthClientInfo> infoList = new ArrayList<>();

        // 구글
        infoList.add(new  OAthClientInfo(
                "google",
                "254999034916-61o7vuis0demhdt8jrb1210d92r8o8nn.apps.googleusercontent.com",
                "http://localhost:8080/api/auth/register"
        ));

        // 카카오
        infoList.add(new  OAthClientInfo(
                "kakao",
                "454f615976d86f74a3fcaabb05dca4d0",
                "http://localhost:8080/api/auth/register"
        ));

        // 네이버 (새로 추가!)
        infoList.add(new  OAthClientInfo(
                "naver",
                "kDWLjaWlRgT9xuspYkRQ",  // 네이버 개발자 센터에서 받은 Client ID로 변경
                "http://localhost:8080/api/auth/register"
        ));

        return infoList;
    }
}