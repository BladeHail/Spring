package com.example.first.controller;

import com.example.first.dto.BoardRequestDto;
import com.example.first.dto.UserRequestDto;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    @GetMapping("/my")
    public ResponseEntity<?> getUser(
            Authentication auth
    ) {
        if(isNotValid(auth)) {
            return new ResponseEntity<>("Unidentified user",HttpStatus.UNAUTHORIZED);
        }
        final String username = auth.getName();
        final User user = userRepository.findByUsername(username).orElseThrow();
        return new ResponseEntity<>(user.toDto(), HttpStatus.OK);
    }
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



    private boolean isNotValid(Authentication auth) {
        return (auth == null || !auth.isAuthenticated());
    }
}
