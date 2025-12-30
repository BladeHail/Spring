package com.example.first.service;

import com.example.first.client.YoutubeSearchClient;
import com.example.first.dto.YoutubeSearchItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YoutubeVideoCollectService {

    private final YoutubeSearchClient searchClient;
    private final YoutubeVideoSaveService saveService;

    public void collectByKeyword(String keyword) {
        List<YoutubeSearchItem> items = searchClient.search(keyword);

        for (YoutubeSearchItem item : items) {
            saveService.saveIfNotExists(
                    item.videoId(),
                    item.title(),
                    item.channelId(),
                    item.channelTitle(),
                    item.publishedAt(),
                    item.thumbnailUrl(),
                    keyword
            );
        }
    }
}