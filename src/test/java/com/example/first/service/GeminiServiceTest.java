package com.example.first.service;

import com.example.first.dto.NaverNewsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        NaverNewsService.class,
        GeminiService.class,
        GeminiServiceTest.TestConfig.class
})
public class GeminiServiceTest {
    @Autowired
    private GeminiService geminiService;
    @Autowired
    private NaverNewsService naverNewsService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RestTemplate newsTemplate() {
            return new RestTemplate();
        }

        @Bean
        public RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }

    @Test
    void GeminiTest() {
        // [Step 1] 네이버 뉴스 검색 (예: "인공지능")
        String keyword = "패럴림픽";
        System.out.println("1. 네이버 뉴스 검색 시작: 키워드 = " + keyword);

        NaverNewsDto newsDto = naverNewsService.searchNews(keyword);

        // 데이터가 잘 왔는지 검증
        assertNotNull(newsDto);
        assertNotNull(newsDto.getItems());

        // [Step 2] 뉴스 내용을 하나의 긴 문자열로 합치기
        // (제미나이한테 "이거 다 읽고 요약해"라고 주기 위해 내용을 뭉칩니다)
        StringBuilder newsContent = new StringBuilder();
        newsContent.append("주제: ").append(keyword).append(" 관련 뉴스 모음\n\n");

        for (var item : newsDto.getItems()) {
            // HTML 태그(<b> 등)가 섞여 있을 수 있어서 정규식으로 제거 (선택사항)
            String cleanTitle = item.getTitle().replaceAll("<[^>]*>", "");
            String cleanDesc = item.getDescription().replaceAll("<[^>]*>", "");

            newsContent.append("제목: ").append(cleanTitle).append("\n");
            newsContent.append("내용: ").append(cleanDesc).append("\n");
            newsContent.append("----------------\n");
        }

        System.out.println("2. 뉴스 데이터 가공 완료 (Gemini에게 보낼 준비 끝)");

        // [Step 3] 제미나이에게 요약 요청 (Service 연동)
        System.out.println("3. Gemini에게 요약 요청 중...");
        String summary = geminiService.getSummary(newsContent.toString());

        // [Step 4] 결과 출력
        System.out.println("\n=============================================");
        System.out.println(" [Gemini AI 뉴스 브리핑]");
        System.out.println("=============================================");
        System.out.println(summary);
        System.out.println("=============================================\n");
    }
}
