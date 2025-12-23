package com.example.first.dto;

import java.time.Instant;

public record YoutubeSearchItem(
        String videoId,
        String title,
        String channelId,
        String channelTitle,
        Instant publishedAt,
        String thumbnailUrl
) {
}

