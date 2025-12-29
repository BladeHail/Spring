package com.example.first.controller;

import com.example.first.dto.response.VideoResponseDto;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.repository.VideoRepository;
import com.example.first.service.VideoAdminService;
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
    private final VideoAdminService adminService;

    @GetMapping("/list")
    public List<VideoResponseDto> list(Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return repository.findAllByOrderByIdDesc().stream()
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
    public record UpdateRequest(
            String youtubeUrl, String keyword
    ) {}

    @PutMapping("/admin/{id}")
    public ResponseEntity<?> update(Authentication auth, @PathVariable Long id,
                                    @RequestBody UpdateRequest request) {
        if (!isAdmin(auth)) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        try {
            adminService.updateVideo(id, request.youtubeUrl(), request.keyword());
            return ResponseEntity.ok("Update OK");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        if (!isAdmin(auth)) return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        try {
            adminService.deleteVideo(id);
            return ResponseEntity.ok("Delete OK");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;

        return userRepository.findByUsername(auth.getName())
                .map(User::isAdmin)
                .orElse(false);
    }
}



