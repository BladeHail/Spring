package com.example.first.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.first.dto.LiveYtDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LiveYtService {
    //@Value 어노테이션을 사용하여 properties에서 정의한 YouTube API 키를 가져옴
    @Value("${youtube.api.key}")
    private String apiKey;

    //쉼표(,)로 구분된 채널 ID들을 리스트로 받음
    @Value("#{'${youtube.target.channel-id}'.split(',')}")
    private List<String> targetChannelIds;

    @Value("#{'${youtube.target.keywords}'.split(',')}")
    private String[] targetKeywords;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 여러 개의 영상을 담을 리스트 (초기화)
    private List<LiveYtDto> currentLiveVideos = new ArrayList<>();

    //키워드 깨짐 확인용 코드
    @PostConstruct
    public void initCheck() {
        log.info("======================================");
        log.info("설정된 키워드 목록: {}", Arrays.toString(targetKeywords));
        log.info("======================================");
    }

    // 1분(60,000ms)마다 실행
    @Scheduled(fixedRate = 60000)
    public void checkYtLiveStatus() {
        List<LiveYtDto> foundVideos = new ArrayList<>();

        for (String channelId : targetChannelIds) {
            try {// 해당 채널의 '실시간' 영상 검색 API 호출
                String targetId = channelId.trim();
                String apiUrl = "https://www.googleapis.com/youtube/v3/search"
                        + "?part=snippet"
                        + "&channelId=" + targetId
                        + "&eventType=live"
                        + "&type=video"
                        + "&key=" + apiKey;

                String response = restTemplate.getForObject(apiUrl, String.class);
                JsonNode items = objectMapper.readTree(response).path("items");

                if (items.size() > 0) {
                    for (JsonNode item : items) {
                        String title = item.path("snippet").path("title").asText();
                        String videoId = item.path("id").path("videoId").asText();

                        String resChannelId = item.path("snippet").path("ChannelId").asText();

                        if (containsKeyword(title)) {
                            // DTO에 채널 ID도 함께 저장
                            foundVideos.add(new LiveYtDto(title, videoId, resChannelId));
                            log.info("채널[{}] 영상 발견: {}", resChannelId, title);
                        }
                    }
                }
            }catch (Exception e) {
                log.error("채널({}) 확인중 오류", channelId, e);
            }
        }
        //채널별로 1개의 영상만 남기도록 필터링 (Stream 활용)
        this.currentLiveVideos = foundVideos.stream()
                .collect(Collectors.toMap(
                        LiveYtDto::getChannelId, // Key: 채널 ID
                        Function.identity(),     // Value: 영상 객체 자체
                        (existing, replacement) -> existing // 중복 시 기존 영상 유지
                        ))
                .values()
                .stream()
                .collect(Collectors.toList());

        if (currentLiveVideos.isEmpty()) {
            log.info("현재 조건에 맞는 방송 없음");
        } else {
            log.info("최종 업데이트: {}개의 영상 (채널별 중복 제거됨)", currentLiveVideos.size());
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