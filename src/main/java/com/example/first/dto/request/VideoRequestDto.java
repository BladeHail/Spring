package com.example.first.dto.request;

public record VideoRequestDto(
                              String youtubeUrl,
                              String keyword   // 예: "올림픽"
) {
}
