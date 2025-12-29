package com.example.first.dto.response;

import com.example.first.entity.Block;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostResponseDto {
    private Long id;

    private Long authorId;

    private String title;

    private List<Block> blocks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean deleted;
}
