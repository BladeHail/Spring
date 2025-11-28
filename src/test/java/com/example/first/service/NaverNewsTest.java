package com.example.first.service;

import com.example.first.dto.NaverNewsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // 스프링 컨테이너를 로드해서 테스트 (설정 파일의 API 키도 다 가져옴)
class NaverNewsTest {

    @Autowired
    private NaverNewsService naverNewsService;

    @Test
    @DisplayName("네이버 뉴스 API가 정상적으로 데이터를 가져와야 한다")
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

}