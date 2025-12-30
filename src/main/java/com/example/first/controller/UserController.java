package com.example.first.controller;

import com.example.first.dto.request.UserRequestDto;
import com.example.first.dto.response.UserResponseDto;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    @GetMapping("/my")
    public ResponseEntity<UserResponseDto> my(Authentication auth) {
        try {
            if(isNotValid(auth)) {
                return new ResponseEntity<>(new UserResponseDto(), HttpStatus.UNAUTHORIZED);
            }
            final User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            return new ResponseEntity<>(user.toDto(), HttpStatus.OK);
        } catch(Exception e) {
            return new ResponseEntity<>(new UserResponseDto(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*@DeleteMapping("/my/deleteAccount")
    public ResponseEntity<String> deleteAccount(Authentication auth) {
        try {
            if(auth == null || !auth.isAuthenticated()) {
                return new ResponseEntity<>("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
            }
            authService.delete(auth.getName());
            return new ResponseEntity<>("탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.", HttpStatus.NO_CONTENT);
        } catch(Exception e) {
            log.error("회원 탈퇴 실패", e);
            return new ResponseEntity<>("토큰 확인 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
    @PutMapping("/my")
    public ResponseEntity<?> updateUser(
            Authentication auth,
            @RequestBody UserRequestDto request
    ) {
        if(isNotValid(auth)) {
            return new ResponseEntity<>("Unidentified user",HttpStatus.UNAUTHORIZED);
        }
        request.setUsername(auth.getName());
        userService.updateUser(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/checkToken")
    public ResponseEntity<String> checkToken(Authentication auth) {
        try {
            if (isNotValid(auth)) {
                System.out.println(auth);
                return new ResponseEntity<>("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>("유효한 JWT", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("토큰 확인 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isNotValid(Authentication auth) {
        return (auth == null || !auth.isAuthenticated());
    }
}
