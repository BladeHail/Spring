package com.example.first.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeSearchService {

    //private final YoutubeVideoCollectService collectService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String keyword = "Olympics"; // TODO: 설정으로 교체

        log.info("Application ready. Start YouTube search. keyword={}", keyword);

        try {
            //collectService.collectByKeyword(keyword);
            log.info("YouTube search finished successfully.");
        } catch (Exception e) {
            log.error("YouTube search failed on startup", e);
        }
    }
}

