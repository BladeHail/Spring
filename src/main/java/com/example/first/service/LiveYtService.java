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

import java.util.*;
import java.util.stream.Collectors;

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

    // 여러 개의 영상을 담는 리스트 (이 리스트만 프런트에 노출됨)
    private volatile List<LiveYtDto> currentLiveVideos = Collections.emptyList();

    @PostConstruct
    public void initCheck() {
        log.info("======================================");
        log.info("설정된 키워드 목록: {}", Arrays.toString(targetKeywords));
        log.info("타겟 채널 목록: {}", targetChannelIds);
        log.info("======================================");

        // 서버 재기동 시, 이미 진행 중인 라이브 복구용 search.list (채널당 1회씩)
        try {
            initCurrentLivesBySearch();
        } catch (Exception e) {
            log.error("초기 라이브 검색 실패: {}", e.getMessage());
        }
    }

    /**
     * 서버 기동 시, 각 채널에 대해 search.list를 1번씩 호출하여
     * "이미 진행 중인 라이브"가 있으면 currentLiveVideos에 채워 넣는다.
     * (Quota: 채널 수 × 100, 서버를 자주 재기동하지 않는 전제에서 충분히 감당 가능)
     */
    private void initCurrentLivesBySearch() throws Exception {
        List<LiveYtDto> found = new ArrayList<>();

        for (String channelId : targetChannelIds) {
            channelId = channelId.trim();
            if (channelId.isEmpty()) continue;
            found.addAll(searchLiveStreamsForChannel(channelId));
        }

        synchronized (this) {
            currentLiveVideos = found;
        }
        log.info("서버 기동 시 초기 라이브 수: {}", currentLiveVideos.size());
    }

    /**
     * 해당 채널의 '실시간' 영상들을 search.list로 모두 검색.
     * (키워드는 title.contains(...) 로 필터링)
     */
    private List<LiveYtDto> searchLiveStreamsForChannel(String channelId) throws Exception {
        String apiUrl = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet"
                + "&channelId=" + channelId
                + "&eventType=live"
                + "&type=video"
                + "&key=" + apiKey;

        String response = ytTemplate.getForObject(apiUrl, String.class);
        JsonNode items = objectMapper.readTree(response).path("items");

        List<LiveYtDto> result = new ArrayList<>();

        if (!items.isEmpty()) {
            for (JsonNode item : items) {
                String title = item.path("snippet").path("title").asText();
                String videoId = item.path("id").path("videoId").asText();
                String resChannelId = item.path("snippet").path("channelId").asText();
                String channelName = item.path("snippet").path("channelTitle").asText();

                if (containsKeyword(title)) {
                    log.info("[초기 검색] 방송 발견 채널: [{}], 제목: [{}]", channelName, title);
                    result.add(new LiveYtDto(title, videoId, resChannelId, channelName));
                }
            }
        }

        return result;
    }

    /**
     * WebSub 콜백에서 "새로운 영상 이벤트"가 들어왔을 때 호출되는 메서드.
     * - title에 키워드가 포함되어 있지 않으면 무시
     * - 키워드가 포함되어 있다면 currentLiveVideos에 upsert
     *   (같은 videoId가 이미 있다면 갱신, 없다면 추가)
     */
    public void registerLiveFromCallback(String title, String videoId, String channelId, String channelName) {
        if (title == null) title = "";
        if (!containsKeyword(title)) {
            log.info("키워드 미일치로 콜백 무시: title={}", title);
            return;
        }

        synchronized (this) {
            List<LiveYtDto> copy = new ArrayList<>(currentLiveVideos);
            // 같은 videoId가 있다면 제거 후 다시 추가 (업데이트 개념)
            copy.removeIf(v -> v.getVideoId().equals(videoId));
            copy.add(new LiveYtDto(title, videoId, channelId, channelName));
            currentLiveVideos = copy;
        }

        log.info("WebSub 이벤트로 라이브 등록/갱신: videoId={}, channelId={}", videoId, channelId);
    }

    /**
     * NOTE: 라이브 유지 상태 확인 주기 (현재 15초).
     * 필요 시 fixedRate 값을 조정해서 간격을 변경할 수 있다.
     * - 현재 저장된 모든 videoId를 한 번에 videos.list로 조회
     * - actualEndTime 이 존재하는 videoId는 "종료"로 판단하여 제거
     */
    @Scheduled(fixedRate = 15000)
    public void refreshLiveStatusWithVideosApi() {
        List<LiveYtDto> snapshot = currentLiveVideos;
        if (snapshot.isEmpty()) {
            return;
        }

        String idsParam = snapshot.stream()
                .map(LiveYtDto::getVideoId)
                .collect(Collectors.joining(","));

        String apiUrl = "https://www.googleapis.com/youtube/v3/videos"
                + "?part=liveStreamingDetails"
                + "&id=" + idsParam
                + "&key=" + apiKey;

        try {
            String response = ytTemplate.getForObject(apiUrl, String.class);
            JsonNode items = objectMapper.readTree(response).path("items");

            // 실제로 아직 라이브 중인 videoId 집합
            Set<String> stillLiveIds = new HashSet<>();

            for (JsonNode item : items) {
                String videoId = item.path("id").asText();
                JsonNode details = item.path("liveStreamingDetails");
                String endTime = details.path("actualEndTime").asText();

                // actualEndTime 이 없거나 빈 문자열이면 아직 라이브 중
                if (endTime == null || endTime.isEmpty()) {
                    stillLiveIds.add(videoId);
                }
            }

            synchronized (this) {
                currentLiveVideos = snapshot.stream()
                        .filter(v -> stillLiveIds.contains(v.getVideoId()))
                        .collect(Collectors.toList());
            }

            log.info("videos.list 기반 라이브 상태 갱신: 유지 {}개", currentLiveVideos.size());
        } catch (Exception e) {
            log.error("videos.list 갱신 실패: {}", e.getMessage());
        }
    }

    // 기존 containsKeyword 로직 그대로 유지
    private boolean containsKeyword(String title) {
        for (String keyword : targetKeywords) {
            if (title.contains(keyword.trim())) return true;
        }
        return false;
    }

    // 컨트롤러에서 사용하는 getter는 그대로 유지
    public List<LiveYtDto> getLiveVideos() {
        return currentLiveVideos;
    }
}
