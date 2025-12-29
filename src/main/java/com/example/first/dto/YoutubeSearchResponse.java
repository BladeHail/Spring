package com.example.first.dto;

import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class YoutubeSearchResponse {

    private List<Item> items;

    @Getter
    public static class Item {
        private Id id;
        private Snippet snippet;
    }

    @Getter
    public static class Id {
        private String videoId;
    }

    @Getter
    public static class Snippet {
        private String title;
        private String channelId;
        private String channelTitle;
        private Instant publishedAt;
        private Thumbnails thumbnails;
    }

    @Getter
    public static class Thumbnails {
        private Thumbnail high;
    }

    @Getter
    public static class Thumbnail {
        private String url;
    }
}


