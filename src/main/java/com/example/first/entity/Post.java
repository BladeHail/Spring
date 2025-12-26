package com.example.first.entity;

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
}
