package com.example.first.entity;

import com.example.first.dto.response.PostResponseDto;
import com.example.first.utils.BlockListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authorId;

    private String title;

    @Convert(converter = BlockListConverter.class)
    @Column(columnDefinition = "json", nullable = false)
    private List<Block> blocks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean deleted;

    public Post(Long authorId, String title, List<Block> blocks) {
        this.authorId = authorId;
        this.title = title;
        this.blocks = blocks;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.deleted = false;
    }

    public Post(Long id, Long authorId, String title, List<Block> blocks, LocalDateTime createdAt, boolean deleted) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.blocks = blocks;
        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.now();
        this.deleted = deleted;
    }

    public void Delete() {
        this.deleted = true;
    }

    public PostResponseDto toDto() {
        PostResponseDto dto = new PostResponseDto();
        dto.setId(id);
        dto.setAuthorId(authorId);
        dto.setTitle(title);
        dto.setBlocks(blocks);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        dto.setDeleted(deleted);
        return dto;
    }
}
