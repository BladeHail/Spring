package com.example.first.entity;

import com.example.first.dto.BoardDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private int views;

    @Column
    private String media;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    public BoardEntity(String title, String content, String author, String media) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.media = media;
    }
    public BoardDto asDto() {
        BoardDto dto = new BoardDto();
        dto.setId(id);
        dto.setTitle(this.title);
        dto.setContent(this.content);
        dto.setAuthor(this.author);
        dto.setMedia(this.media);
        dto.setViews(this.views);
        dto.setCreatedAt(this.createdAt);
        dto.setUpdatedAt(this.updatedAt);
        return dto;
    }
}
