package com.example.first.service;

import com.example.first.dto.LiveYtDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LiveYtService {
    //@Value 어노테이션을 사용하여 properties에서 정의한 YouTube API 키를 가져옴
    @Value("${youtube.api.key}")
    private String apiKey;

    //쉼표(,)로 구분된 채널 ID들을 리스트로 받음
    @Value("#{'${youtube.target.channel-id}'.split(',')}")
    private List<String> targetChannelIds;

    @Value("#{'${youtube.target.keywords}'.split(',')}")
    private String[] targetKeywords;

    //Config에서 주입받음
    private final RestTemplate ytTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 여러 개의 영상을 담을 리스트 (초기화)
    private volatile List<LiveYtDto> currentLiveVideos = Collections.emptyList();

    //키워드 깨짐 확인용 코드
    @PostConstruct
    public void initCheck() {
        log.info("======================================");
        log.info("설정된 키워드 목록: {}", Arrays.toString(targetKeywords));
        log.info("======================================");
    }

    // 10분(600,000ms)마다 실행
    @Scheduled(fixedRate = 600000)
    public void checkYtLiveStatus() {
        List<LiveYtDto> foundVideos = new ArrayList<>();

        for (String channelId : targetChannelIds) {
            try {
                // 아래 메서드 호출
                LiveYtDto video = fetchLiveVideo(channelId.trim());
                if (video != null) {
                    foundVideos.add(video);
                }
            } catch (Exception e) {
                log.error("채널({}) 조회 실패: {}", channelId, e.getMessage());
            }
        }

        this.currentLiveVideos = foundVideos;
        log.info("라이브 영상 갱신 완료: 총 {}개", currentLiveVideos.size());
    }
            // 해당 채널의 '실시간' 영상 검색 API 호출
                private LiveYtDto fetchLiveVideo(String channelId) throws Exception {
                String apiUrl = "https://www.googleapis.com/youtube/v3/search"
                        + "?part=snippet"
                        + "&channelId=" + channelId
                        + "&eventType=live"
                        + "&type=video"
                        + "&key=" + apiKey;

                String response = ytTemplate.getForObject(apiUrl, String.class);
                JsonNode items = objectMapper.readTree(response).path("items");

                if (items.size() > 0) {
                    for (JsonNode item : items) {
                        String title = item.path("snippet").path("title").asText();
                        String videoId = item.path("id").path("videoId").asText();
                        String resChannelId = item.path("snippet").path("channelId").asText();
                        String channelName = item.path("snippet").path("channelTitle").asText();

                        // 디버깅용 로그: ID가 제대로 찍히는지 눈으로 확인하세요
                        // log.info("채널ID 추출 확인: {}", resChannelId);
                        if (containsKeyword(title)) {
                            log.info("방송 발견 채널: [{}], 제목: [{}]", channelName, title);
                            return new LiveYtDto(title, videoId, resChannelId, channelName);
                        }
                    }
                }
                return null;
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