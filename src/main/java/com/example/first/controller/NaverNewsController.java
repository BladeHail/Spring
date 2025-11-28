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
            "패럴림픽 (국가대표 | 메달 | \"한국 신기록\" | \"대한민국 성적\" | 장소 | 참가인원 | 마스코트 | 일정) -올림픽대로 -공원 -아파트 -서울올림파크텔 -올림픽공원역 -올림픽홀 -연금";

    @GetMapping("/api/news/olympic")
    public NaverNewsDto getNews() {
        return naverNewsService.searchNews(OLYMPIC_KEYWORD);
    }
}
