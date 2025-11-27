package com.example.first.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.first.dto.LiveYtDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LiveYtService {
    //@Value 어노테이션을 사용하여 properties에서 정의한 YouTube API 키를 가져옴
    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.target.channel-id}")
    private String targetChannelId;

    @Value("#{'${youtube.target.keywords}'.split(',')}")
    private String[] targetKeywords;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 여러 개의 영상을 담을 리스트 (초기화)
    private List<LiveYtDto> currentLiveVideos = new ArrayList<>();

    // 1분(60,000ms)마다 실행
    @Scheduled(fixedRate = 60000)
    public void checkYtLiveStatus() {
        List<LiveYtDto> foundVideos = new ArrayList<>();

        try {// 해당 채널의 '실시간' 영상 검색 API 호출
            String apiUrl = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet"
                    + "&channelId=" + targetChannelId
                    + "&eventType=live"
                    + "&type=video"
                    + "&key=" + apiKey;

            String response = restTemplate.getForObject(apiUrl, String.class);
            JsonNode items = objectMapper.readTree(response).path("items");

            if (items.size() > 0) {
                for (JsonNode item : items) {
                    String title = item.path("snippet").path("title").asText();
                    String videoId = item.path("id").path("videoId").asText();

                    if (containsKeyword(title)) {
                        foundVideos.add(new LiveYtDto(title, videoId));
                        log.info("송출 대상 발견: {}", title);
                    }
                }
            }

            this.currentLiveVideos = foundVideos;

            if (currentLiveVideos.isEmpty()) {
                // 정보성 로그
                log.info("현재 조건에 맞는 방송 없음");
            } else {
                log.info("총 {}개의 영상 송출 중", currentLiveVideos.size());
            }

        } catch (Exception e) {
            // 에러 로그 (e.getMessage() 대신 e 객체 자체를 넘겨서 스택트레이스 확인 가능)
            log.error("API 호출 오류", e);
            this.currentLiveVideos.clear();
        }
    }

    private boolean containsKeyword(String title) {
        for (String keyword : targetKeywords) {
            if (title.contains(keyword.trim())) return true;
        }
        return false;
    }

    public List<LiveYtDto> getLiveVideos() {
        return currentLiveVideos;
    }
}