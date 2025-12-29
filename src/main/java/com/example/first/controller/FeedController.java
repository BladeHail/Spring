package com.example.first.controller;

import com.example.first.dto.response.FeedItemDto;
import com.example.first.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;
    @GetMapping
    public ResponseEntity<List<FeedItemDto>> getFeed() {
        return new ResponseEntity<>(feedService.getFeeds(), HttpStatus.OK);
    }
}
