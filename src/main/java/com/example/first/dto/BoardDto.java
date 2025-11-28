package com.example.first.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDto {
    private Long id;
    private String title;
    private String content;
    private String author;
    private int views;
    private String media;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
