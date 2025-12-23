package com.example.first.service;

import com.example.first.client.YoutubeVideoClient;
import com.example.first.dto.YoutubeVideoDetail;
import com.example.first.utils.YoutubeUrlParser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoRegisterService {

    private final YoutubeVideoClient videoClient;
    private final YoutubeVideoSaveService saveService;

    @Transactional
    public void register(String youtubeUrl, String keyword) {

        YoutubeVideoDetail detail =
                videoClient.fetchByUrl(youtubeUrl);

        saveService.saveIfNotExists(
                detail.videoId(),
                detail.title(),
                detail.channelId(),
                detail.channelTitle(),
                detail.publishedAt(),
                detail.thumbnailUrl(),
                keyword
        );
    }
}

