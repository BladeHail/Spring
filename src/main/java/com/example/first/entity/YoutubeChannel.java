package com.example.first.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "youtube_channel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubeChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String channelId;

    @Column(nullable = false)
    private String name;

    public static YoutubeChannel of(String channelId, String name) {
        YoutubeChannel c = new YoutubeChannel();
        c.channelId = channelId;
        c.name = name;
        return c;
    }
}

