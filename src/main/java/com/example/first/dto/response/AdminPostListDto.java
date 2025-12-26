package com.example.first.dto.response;

import java.time.LocalDateTime;

public record AdminPostListDto(
        Long id,
        String title,
        String authorName,
        LocalDateTime createdAt,
        boolean deleted
) {
}
