package com.example.first.controller;

import com.example.first.dto.response.VideoResponseDto;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.repository.VideoRepository;
import com.example.first.service.VideoRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/videos")
public class YoutubeVideoController {
    private final VideoRegisterService registerService;
    private final VideoRepository repository;
    private final UserRepository userRepository;

    @GetMapping("/list")
    public List<VideoResponseDto> list(Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return repository.findAll().stream()
                .map(VideoResponseDto::from)
                .toList();
    }

    @PostMapping("/admin")
    public ResponseEntity<?> register(
            Authentication auth,
            @RequestBody RegisterRequest request
    ) {
        if(auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> user = userRepository.findByUsername(auth.getName());
        if(user.isEmpty() || !user.get().isAdmin()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        registerService.register(
                request.youtubeUrl(),
                request.keyword()
        );
        return new ResponseEntity<>("OK", HttpStatus.CREATED);
    }

    public record RegisterRequest(
            String youtubeUrl,
            String keyword
    ) {}
}

