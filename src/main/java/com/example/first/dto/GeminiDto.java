package com.example.first.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.mortbay.jetty.ResourceCache;

import java.util.List;

public class GeminiDto {

    // [요청 DTO]
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private List<Content> contents;
    }

    // [응답 DTO]
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    // 구글이 usageMetadata 같은 걸 더 보내도 에러 내지 말고 무시
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private List<Candidate> candidates;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;
    }
}