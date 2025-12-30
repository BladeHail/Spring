package com.example.first.controller;

import com.example.first.dto.response.FeedItemDto;
import com.example.first.entity.User;
import com.example.first.service.FeedService;
import com.example.first.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;
    private final UserService userService;
    @GetMapping
    public ResponseEntity<List<FeedItemDto>> getFeed() {
        return new ResponseEntity<>(feedService.getFeeds(), HttpStatus.OK);
    }
    @DeleteMapping
    public ResponseEntity<?> removeFeed(Authentication auth,
                                        @RequestBody FeedItemDto dto) {
        if(notAdmin(auth)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        feedService.removeFeed(dto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PutMapping
    public ResponseEntity<?> updateFeed(Authentication auth) {
        if(notAdmin(auth)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        feedService.updateFeed(10);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private boolean notAdmin(Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            return true;
        }
        Optional<User> user = userService.findUserByUsername(auth.getName());
        return user.isEmpty() || !user.get().isAdmin();
    }
}
