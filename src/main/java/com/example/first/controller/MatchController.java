package com.example.first.controller;


import com.example.first.dto.request.MatchRequestDto;
import com.example.first.entity.User;
import com.example.first.service.MatchService;
import com.example.first.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchController {
    private final UserService userService;
    private final MatchService matchService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getMatch(
            Authentication auth,
            @PathVariable Long id
    ){
        if(auth == null || !auth.isAuthenticated()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(matchService.getMatchById(id), HttpStatus.OK);
    }
    @PostMapping
    public ResponseEntity<?> newMatch(Authentication auth,
                                         @RequestBody MatchRequestDto requestDto
    ) {
        if(auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>("NO", HttpStatus.UNAUTHORIZED);
        }
        String username = auth.getName();
        Optional<User> user = userService.findUserByUsername(username);
        if(user.isPresent() && user.get().isAdmin()) {
            matchService.createMatch(requestDto);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>("?", HttpStatus.UNAUTHORIZED);
    }
    @PutMapping
    public ResponseEntity<?> modifyMatch(Authentication auth,
                                         @RequestBody MatchRequestDto requestDto
    ) {
        if(auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>("NO", HttpStatus.UNAUTHORIZED);
        }
        String username = auth.getName();
        Optional<User> user = userService.findUserByUsername(username);
        if(user.isPresent() && user.get().isAdmin()) {
            matchService.updateMatch(requestDto);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>("?", HttpStatus.UNAUTHORIZED);
    }
    // 결산 기능 추가하기(포인트 변동)
}
