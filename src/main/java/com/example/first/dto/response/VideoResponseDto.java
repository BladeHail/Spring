package com.example.first.dto.response;

import com.example.first.entity.YoutubeVideo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoResponseDto {
    private Long id;
    private String videoId;
    private String title;
    private String thumbnailUrl;
    private String keyword;

    public static VideoResponseDto from(YoutubeVideo entity) {
        return VideoResponseDto.builder()
                .id(entity.getId())
                .videoId(entity.getVideoId())
                .title(entity.getTitle())
                .thumbnailUrl(entity.getThumbnailUrl())
                .keyword(entity.getKeyword())
                .build();
    }
}

