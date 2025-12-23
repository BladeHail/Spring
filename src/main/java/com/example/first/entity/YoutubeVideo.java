package com.example.first.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "youtube_video",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "videoId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubeVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** YouTube videoId */
    @Column(nullable = false, length = 20)
    private String videoId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String thumbnailUrl;

    @Column(nullable = false, length = 50)
    private String channelId;

    @Column(nullable = false)
    private String channelTitle;

    @Column(nullable = false)
    private Instant publishedAt;

    /** 최초 수집 시각, 필요는 없을 테지만 일단 사용 */
    @Column(nullable = false)
    private Instant collectedAt;

    /** 어떤 키워드로 수집되었는지, 필요는 없을 테지만 일단 사용 */
    @Column(nullable = false, length = 100)
    private String keyword;

    public static YoutubeVideo create(
            String videoId,
            String title,
            String channelId,
            String channelTitle,
            Instant publishedAt,
            String thumbnailUrl,
            String keyword
    ) {
        YoutubeVideo video = new YoutubeVideo();
        video.videoId = videoId;
        video.title = title;
        video.channelId = channelId;
        video.channelTitle = channelTitle;
        video.publishedAt = publishedAt;
        video.thumbnailUrl = thumbnailUrl;
        video.collectedAt = Instant.now();
        video.keyword = keyword;
        return video;
    }

}

