package com.example.first.dto;

import java.time.Instant;

public record YoutubeVideoDetail(
        String videoId,
        String title,
        String channelId,
        String channelTitle,
        Instant publishedAt,
        String thumbnailUrl
) {
}

