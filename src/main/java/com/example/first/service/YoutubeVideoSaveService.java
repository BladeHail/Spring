package com.example.first.service;

import com.example.first.entity.YoutubeVideo;
import com.example.first.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class YoutubeVideoSaveService {

    private final VideoRepository repository;

    @Transactional
    public void saveIfNotExists(
            String videoId,
            String title,
            String channelId,
            String channelTitle,
            Instant publishedAt,
            String keyword,
            String thumbnail
    ) {
        if (repository.existsByVideoId(videoId)) {
            return;
        }

        YoutubeVideo video = YoutubeVideo.create(
                videoId,
                title,
                channelId,
                channelTitle,
                publishedAt,
                keyword,
                thumbnail
        );

        repository.save(video);
    }
}

