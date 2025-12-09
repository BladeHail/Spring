package com.example.first.controller;

import com.example.first.dto.NaverNewsDto;
import com.example.first.service.NaverNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NaverNewsController {
    private final NaverNewsService naverNewsService;

    // 복잡한 검색어 조합을 상수로 선언
    private static final String OLYMPIC_KEYWORD =
            "패럴림픽";

    @GetMapping("/api/news/olympic")
    public NaverNewsDto getNews() {
        return naverNewsService.searchNews(OLYMPIC_KEYWORD);
    }
}
