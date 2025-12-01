package com.example.first.service;

import com.example.first.dto.GeminiDto;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private final RestClient.Builder restClientBuilder;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    //요청 내용 만들기
    public String getSummary(String newsBody) {
        // [1] 프롬프트 만들기
        String prompt = "다음 뉴스를 3줄로 요약해줘: \n" + newsBody;

        // [2] DTO 만들기
        GeminiDto.Request request = GeminiDto.Request.builder()
                .contents(List.of(
                        GeminiDto.Content.builder()
                                .parts(List.of(GeminiDto.Part.builder().text(prompt).build()))
                                .build()
                ))
                .build();

        // [3] URL 강제 조립 (핵심)
        String finalUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-09-2025:generateContent?key=" + apiKey;

        // 디버깅용 로그 (콘솔을 확인)
        System.out.println("요청 URL: " + finalUrl);

        // [4] API 호출
        // RestClient.create()를 써서 아주 깨끗한 클라이언트를 새로 만듬
        RestClient tempClient = RestClient.create();

        GeminiDto.Response response = tempClient.post()
                .uri(URI.create(finalUrl)) // ★ URI.create()로 넣으면 스프링이 인코딩을 안 합니다.
                .body(request)
                .retrieve()
                .body(GeminiDto.Response.class);

        // [5] 결과 반환
        return response.getCandidates().get(0)
                .getContent()
                .getParts().get(0)
                .getText();
    }
}
