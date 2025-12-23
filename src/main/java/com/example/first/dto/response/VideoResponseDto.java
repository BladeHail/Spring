package com.example.first.dto.response;

import com.example.first.entity.YoutubeVideo;

public record VideoResponseDto(
        String videoId,
        String title,
        String thumbnailUrl
) {
    public static VideoResponseDto from(YoutubeVideo video) {
        return new VideoResponseDto(
                video.getVideoId(),
                video.getTitle(),
                video.getThumbnailUrl()
        );
    }
}

