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
// ... (기존 import 및 클래스 선언 동일)
@Service
@Slf4j
@RequiredArgsConstructor
public class LiveYtService {

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("#{'${youtube.target.channel-id}'.split(',')}")
    private List<String> targetChannelIds;

    @Value("#{'${youtube.target.keywords}'.split(',')}")
    private String[] targetKeywords;

    private final RestTemplate ytTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 기존 그대로 유지
    private volatile List<LiveYtDto> currentLiveVideos = Collections.emptyList();

    @PostConstruct
    public void initCheck() {
        log.info("======================================");
        log.info("설정된 키워드 목록: {}", Arrays.toString(targetKeywords));
        log.info("======================================");
    }
    @Scheduled(fixedRate = 10000)
    public void checkYtLiveStatus() {
        System.out.println("라이브 영상의 갱신을 시작합니다.");

        List<LiveYtDto> foundVideos = new ArrayList<>();

        for (String channelId : targetChannelIds) {
            try {
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
    /*
     * ============================
     *   쿼터 절감 로직 삽입 부분
     * ============================
     */
    private LiveYtDto fetchLiveVideo(String channelId) throws Exception {

        // 1) 기존에 이 채널의 라이브가 이미 저장되어 있다면 videos.list 로 상태 확인
        LiveYtDto existing = currentLiveVideos.stream()
                .filter(v -> v.getChannelId().equals(channelId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            if (isStillLive(existing.getVideoId())) {
                // 방송 계속 중 → 그대로 반환
                return existing;
            } else {
                // 라이브 종료됨 → search.list 재조회 필요
                log.info("기존 방송 종료됨: {}", existing.getVideoId());
            }
        }

        // 2) search.list (비용 100)
        return searchLiveStream(channelId);
    }
    /*
     * videos.list 기반 라이브 유지 여부 확인 (비용 1)
     */
    private boolean isStillLive(String videoId) throws Exception {

        String apiUrl = "https://www.googleapis.com/youtube/v3/videos"
                + "?part=liveStreamingDetails"
                + "&id=" + videoId
                + "&key=" + apiKey;

        String response = ytTemplate.getForObject(apiUrl, String.class);
        JsonNode details = objectMapper.readTree(response)
                .path("items")
                .path(0)
                .path("liveStreamingDetails");

        // actualEndTime 이 존재하면 라이브 종료
        String endTime = details.path("actualEndTime").asText();
        return endTime == null || endTime.isEmpty();
    }
    /*
     * search.list 기반 최초 라이브 검색 (당신의 기존 코드 유지)
     * 비용이 큰 부분이지만 "필요할 때만" 실행됨
     */
    private LiveYtDto searchLiveStream(String channelId) throws Exception {

        String apiUrl = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet"
                + "&channelId=" + channelId
                + "&eventType=live"
                + "&type=video"
                + "&key=" + apiKey;

        String response = ytTemplate.getForObject(apiUrl, String.class);
        JsonNode items = objectMapper.readTree(response).path("items");

        if (!items.isEmpty()) {
            for (JsonNode item : items) {

                String title = item.path("snippet").path("title").asText();
                String videoId = item.path("id").path("videoId").asText();
                String resChannelId = item.path("snippet").path("channelId").asText();
                String channelName = item.path("snippet").path("channelTitle").asText();

                // 기존 containsKeyword 로직 그대로
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
