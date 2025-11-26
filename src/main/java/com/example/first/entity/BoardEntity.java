package com.example.first.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private String author;

    @Column
    private String media;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public BoardEntity() {}

    public BoardEntity(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.media = media;
    }
}
