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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;
    static final String PROCESSING = "__PROCESSING__";
    Map<String, String> summaryCache = new ConcurrentHashMap<>();


    //요청 내용 만들기
    public String getSummary(String newsUrl) {

        // [0] 캐시 먼저 확인
        String cached = summaryCache.get(newsUrl);

        if (cached != null) {
            if (PROCESSING.equals(cached)) {
                return "요약 중입니다. 잠시 후 다시 시도해주세요.";
            }
            return cached; // 이미 요약 완료
        }

        // [1] 최초 요청자만 PROCESSING 등록
        String prev = summaryCache.putIfAbsent(newsUrl, PROCESSING);
        if (prev != null) {
            // 거의 동시에 들어온 요청
            if (PROCESSING.equals(prev)) {
                return "요약 중입니다. 잠시 후 다시 시도해주세요.";
            }
            return prev;
        }

        try {
            // [2] 뉴스 본문 가져오기
            String newsBody = getNewsFromUrl(newsUrl);
            if (newsBody.isEmpty()) {
                summaryCache.remove(newsUrl);
                return "요약이 없습니다";
            }

            // [3] 프롬프트
            String prompt =
                    "다음 링크의 뉴스를 3줄로 요약해줘. " +
                            "각 문장의 앞에는 번호가(1. 2. 3.) 붙어있어야 하고, " +
                            "각 문장 사이에는 간격을 두어야 해.\n" +
                            newsBody;

            GeminiDto.Request request = GeminiDto.Request.builder()
                    .contents(List.of(
                            GeminiDto.Content.builder()
                                    .parts(List.of(
                                            GeminiDto.Part.builder()
                                                    .text(prompt)
                                                    .build()
                                    ))
                                    .build()
                    ))
                    .build();

            String finalUrl =
                    "https://generativelanguage.googleapis.com/v1beta/models/" +
                            "gemini-2.5-flash-preview-09-2025:generateContent?key=" + apiKey;

            RestClient client = RestClient.create();

            GeminiDto.Response response = client.post()
                    .uri(URI.create(finalUrl))
                    .body(request)
                    .retrieve()
                    .body(GeminiDto.Response.class);

            String summary = null;
            if (response != null) {
                summary = response.getCandidates().getFirst()
                        .getContent()
                        .getParts().getFirst()
                        .getText();
            }

            // [4] 캐시에 최종 저장
            summaryCache.put(newsUrl, summary);
            return summary;

        } catch (Exception e) {
            // 실패 시 캐시 정리 (다시 시도 가능)
            summaryCache.remove(newsUrl);
            return "요약 중 오류가 발생했습니다.";
        }
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
