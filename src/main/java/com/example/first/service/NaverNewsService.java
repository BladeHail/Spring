package com.example.first.service;

import com.example.first.dto.NaverNewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.apache.commons.text.StringEscapeUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NaverNewsService {

    private final RestTemplate newsTemplate;

    //프로퍼티스에서 키 가져오기
    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.url.search.news}")
    private String naverUrl;

    public NaverNewsDto searchNews(String keyword) {
        // 1. URI 생성 (쿼리 파라미터 인코딩 처리)
        URI uri = UriComponentsBuilder.fromHttpUrl(naverUrl)
                .queryParam("query", keyword)
                .queryParam("display", 10) //뉴스를 보여주는 위치에 따라 보여줄 뉴스 개수 변경
                .queryParam("sort", "date") //최신순 정렬
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        // 2. 요청 헤더 설정 (Client ID, Secret)
        RequestEntity<Void> req = RequestEntity
                .get(uri)
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .build();

        // 3. API 호출 및 응답 파싱
        ResponseEntity<NaverNewsDto> response = newsTemplate.exchange(req, NaverNewsDto.class);
        NaverNewsDto dto = response.getBody(); // 응답 내용을 변수에 담기

        // 4. 중복 제거 및 데이터 가공 로직
        if (dto != null && dto.getItems() != null) {
            // 중복되지 않은 뉴스만 담을 새로운 리스트
            List<NaverNewsDto.Item> filteredItems = new ArrayList<>();
            // 이미 나온 제목을 기억할 Set
            Set<String> seenTitles = new HashSet<>();

            int id = 0;

            for (NaverNewsDto.Item item : dto.getItems()) {

                if (filteredItems.size() >= 5) {
                    break;
                }

                // (1) HTML 태그 제거 및 디코딩 (비교를 위해 먼저 수행)
                String cleanTitle = item.getTitle().replaceAll("<[^>]*>", "");
                cleanTitle = StringEscapeUtils.unescapeHtml4(cleanTitle);

                String cleanDesc = item.getDescription().replaceAll("<[^>]*>", "");
                cleanDesc = StringEscapeUtils.unescapeHtml4(cleanDesc);

                // (2) 중복 검사: 이미 본 제목이면 건너뛰기
                if (seenTitles.contains(cleanTitle)) {
                    continue;
                }

                // (3) 새로운 뉴스라면 Set에 추가하고 리스트에 담기
                seenTitles.add(cleanTitle); // 제목 기억하기

                item.setTitle(cleanTitle);
                item.setDescription(cleanDesc);
                item.setId(id++); // 중복이 아닌 경우에만 ID 증가

                filteredItems.add(item); // 결과 리스트에 추가
            }

            // (4) 원본 리스트를 중복 제거된 리스트로 교체
            dto.setItems(filteredItems);
        }

        return dto;
    }
}
