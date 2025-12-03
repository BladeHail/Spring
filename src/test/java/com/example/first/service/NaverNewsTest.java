package com.example.first.service;

import com.example.first.config.NaverNewsConfig;
import com.example.first.dto.NaverNewsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest (classes = {
        NaverNewsService.class,
        RestTemplateAutoConfiguration.class,
        NaverNewsConfig.class})
class NaverNewsTest {

    @Autowired
    private NaverNewsService naverNewsService;

    @Test
    @DisplayName("네이버 뉴스 API 데이터")
    void searchNewsTest() {
        // given (준비)
        String keyword = "올림픽";

        // when (실행)
        NaverNewsDto result = naverNewsService.searchNews(keyword);

        // then (검증)
        System.out.println("=== 검색 결과 확인 ===");
        if (result != null && result.getItems() != null) {
            result.getItems().forEach(item -> {
                System.out.println("제목: " + item.getTitle());
                System.out.println("날짜: " + item.getPubDate());
                System.out.println("-------------------------");
            });
        }

        // 1. 결과 객체가 null이 아니어야 함
        assertThat(result).isNotNull();
        // 2. 뉴스 기사 리스트가 비어있지 않아야 함
        assertThat(result.getItems()).isNotEmpty();
        // 3. 가져온 뉴스의 제목에 키워드('올림픽')나 관련 단어가 포함되어 있어야 함
        assertThat(result.getItems().get(0).getTitle()).contains("올림픽");
    }
    @Test
    @DisplayName("네이버 뉴스 검색 및 ID 생성")
    void NewsIdTest() {
        // given (준비)
        String keyword = "올림픽";

        // when (실행 - 실제 API를 호출)
        NaverNewsDto result = naverNewsService.searchNews(keyword);

        // then (검증)

        // 1. 데이터가 비어있지 않은지 기초 검사
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isNotEmpty();

        // 2. 첫 번째 뉴스를 꺼내서 자세히 확인
        NaverNewsDto.Item firstItem = result.getItems().get(0);

        System.out.println("=== ID 생성 확인 로그 ===");
        System.out.println("뉴스 제목: " + firstItem.getTitle());
        System.out.println("생성된 ID: " + firstItem.getId());
        System.out.println("=======================");

        // ID가 null이 아니고, 빈 문자열("")도 아닌지 검사
        assertThat(firstItem.getId()).isNotNull();
        assertThat(firstItem.getId()).isNotEmpty();
    }

}