package com.example.first.client;

import com.example.first.dto.YoutubeVideoDetail;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static com.example.first.utils.YoutubeUrlParser.extractVideoId;

@Component
public class YoutubeVideoClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.api.key}")
    private String apiKey;

    public YoutubeVideoDetail fetchByUrl(String youtubeUrl) {
        String videoId = extractVideoId(youtubeUrl);

        String url = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("part", "snippet")
                .queryParam("id", videoId)
                .queryParam("key", apiKey)
                .toUriString();

        JsonNode root = restTemplate.getForObject(url, JsonNode.class);
        JsonNode item = root.path("items").get(0);
        JsonNode snippet = item.path("snippet");

        return new YoutubeVideoDetail(
                videoId,
                snippet.path("title").asText(),
                snippet.path("channelId").asText(),
                snippet.path("channelTitle").asText(),
                Instant.parse(snippet.path("publishedAt").asText()),
                snippet.path("thumbnails").path("high").path("url").asText()
        );
    }

    public YoutubeVideoDetail fetch(String videoId) {

        String url = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("part", "snippet")
                .queryParam("id", videoId)
                .queryParam("key", apiKey)
                .toUriString();

        YoutubeApiResponse response =
                restTemplate.getForObject(url, YoutubeApiResponse.class);

        YoutubeApiItem item = Objects.requireNonNull(response).items.getFirst();

        return new YoutubeVideoDetail(
                item.id,
                item.snippet.title,
                item.snippet.channelId,
                item.snippet.channelTitle,
                item.snippet.publishedAt,
                item.snippet.thumbnails.high.url
        );
    }

    /** 🔒 YouTube 통신 전용, 외부 노출 금지 */
    private static class YoutubeApiResponse {
        List<YoutubeApiItem> items;
    }

    private static class YoutubeApiItem {
        String id;
        YoutubeApiSnippet snippet;
    }

    private static class YoutubeApiSnippet {
        String title;
        String channelId;
        String channelTitle;
        Instant publishedAt;
        YoutubeApiThumbnails thumbnails;
    }

    private static class YoutubeApiThumbnails {
        YoutubeApiThumbnail high;
    }

    private static class YoutubeApiThumbnail {
        String url;
    }
}



