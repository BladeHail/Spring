package com.example.first.client;

import com.example.first.dto.YoutubeSearchItem;
import com.example.first.dto.YoutubeSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
public class YoutubeSearchClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.api.key}")
    private String apiKey;

    public List<YoutubeSearchItem> search(String keyword) {

        String url = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("order", "date")
                .queryParam("safeSearch", "strict")
                .queryParam("maxResults", 5)
                .queryParam("relevanceLanguage", "ko")
                .queryParam("videoDuration", "medium")
                .queryParam("q", keyword)
                .queryParam("key", apiKey)
                .toUriString();

        YoutubeSearchResponse response =
                restTemplate.getForObject(url, YoutubeSearchResponse.class);

        if (response == null || response.getItems() == null) {
            return List.of();
        }

        return response.getItems().stream()
                .map(this::toItem)
                .toList();
    }

    private YoutubeSearchItem toItem(YoutubeSearchResponse.Item item) {
        return new YoutubeSearchItem(
                item.getId().getVideoId(),
                item.getSnippet().getTitle(),
                item.getSnippet().getChannelId(),
                item.getSnippet().getChannelTitle(),
                item.getSnippet().getPublishedAt(),
                item.getSnippet().getThumbnails().getHigh().getUrl()
        );
    }
}

