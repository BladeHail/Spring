package com.example.first.service;

import com.example.first.dto.GeminiDto;
import lombok.RequiredArgsConstructor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
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
    public String getSummary(String newsUrl) {
        String newsBody = getNewsFromUrl(newsUrl);
        if(newsBody.isEmpty()) {
            System.out.println("뉴스 url에서 뉴스 전문을 가져올 수 없었습니다.");
            return "요약이 없습니다";
        }
        // [1] 프롬프트 만들기
        String prompt = "다음 링크의 뉴스를 3줄로 요약해줘: \n" + newsBody;

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
    public String getNewsFromUrl(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();

            // 1) 불필요한 태그 제거
            doc.select("script, style, nav, header, footer, ads, iframe").remove();

            // 2) 본문일 가능성이 높은 태그 후보
            String[] candidates = { "article", "#content", ".content", ".article", ".news", ".article-body" };

            // 우선적으로 지정 후보 탐색
            for (String selector : candidates) {
                Element el = doc.selectFirst(selector);
                if (el != null) {
                    String text = el.text().trim();
                    if (text.length() > 300) { // 최소 길이 기준
                        return text;
                    }
                }
            }

            // 3) fallback: 텍스트가 가장 긴 block-level 요소를 본문으로 판단
            Element best = null;
            int bestLength = 0;

            for (Element el : doc.body().select("*")) {
                int len = el.text().length();
                if (len > bestLength) {
                    bestLength = len;
                    best = el;
                }
            }

            if (best != null) {
                return best.text();
            }
            System.out.println(url + " 에서 본문을 추출할 수 없었습니다.");
            return "";

        } catch (IOException e) {
            return "(오류 발생: " + e.getMessage() + ")";
        }
    }
}
