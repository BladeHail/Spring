package com.example.first.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeUrlParser {

    private static final Pattern VIDEO_ID_PATTERN =
            Pattern.compile("(?:v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");

    public static String extractVideoId(String url) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }
        return matcher.group(1);
    }
}

