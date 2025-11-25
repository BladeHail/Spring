package com.example.first.controller;

import com.example.first.dto.LiveYtDto;
import com.example.first.service.LiveYtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class LiveYtController {

    private final LiveYtService liveYtService;

    @GetMapping("/api/live-status")
    public Map<String, Object> checkLiveStatus() {
        Map<String, Object> response = new HashMap<>();

        // 서비스에서 현재 방송 목록 가져오기
        List<LiveYtDto> videos = liveYtService.getLiveVideos();

        // isLive: 영상이 하나라도 있으면 true
        response.put("isLive", !videos.isEmpty());
        // videos: 영상 리스트 전체 ([{title:..., videoId:...}, ...])
        response.put("videos", videos);

        return response;
    }
}