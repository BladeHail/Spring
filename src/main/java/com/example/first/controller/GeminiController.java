package com.example.first.controller;

import com.example.first.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GeminiController {
    private final GeminiService geminiService;

    @GetMapping("/api/gemini-summary")
    public String getNewSummary(@RequestParam("url") String newsUri){
        return geminiService.getSummary(newsUri);
    }
}
