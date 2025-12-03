package com.example.first.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class YtSubscriptionService {

    // 감시 대상 채널 (LiveYtService와 동일한 설정 사용)
    @Value("#{'${youtube.target.channel-id}'.split(',')}")
    private List<String> targetChannelIds;

    // ngrok 외부 공개 URL (application.yml 또는 properties 에 추가 필요)
    // app.public-base-url: https://underminingly-semineutral-natacha.ngrok-free.dev
    @Value("${server.base-url}")
    private String publicBaseUrl;

    private final RestTemplate ytTemplate;

    private static final String HUB_URL = "https://pubsubhubbub.appspot.com/subscribe";

    @EventListener(ApplicationReadyEvent.class)
    public void subscribeAllChannelsAtStartup() {
        List<String> channels = targetChannelIds.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for (String channelId : channels) {
            subscribeChannel(channelId);
        }
    }

    private void subscribeChannel(String channelId) {
        String callbackUrl = publicBaseUrl + "/api/yt/callback";
        String topicUrl = "https://www.youtube.com/xml/feeds/videos.xml?channel_id=" + channelId;

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("hub.callback", callbackUrl);
        body.add("hub.mode", "subscribe");
        body.add("hub.topic", topicUrl);
        body.add("hub.verify", "async");
        body.add("hub.lease_seconds", "864000"); // 10일
        body.add("hub.verify_token", "yt_live_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ytTemplate.postForEntity(HUB_URL, request, String.class);
            log.info("YouTube WebSub 구독 요청 완료: channelId={}, callback={}", channelId, callbackUrl);
        } catch (Exception e) {
            log.error("YouTube WebSub 구독 요청 실패: channelId={}, error={}", channelId, e.getMessage());
        }
    }
}
