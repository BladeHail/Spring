package com.example.first.dto.response;

import java.time.LocalDateTime;

public record PostListDto(
        Long id,
        String title,
        String authorName,
        LocalDateTime createdAt
) {
}

