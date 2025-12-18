package com.example.first.service;

import com.example.first.dto.NaverNewsDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaccardSimilarity;
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
import java.util.List;

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
                .queryParam("display", 40) //뉴스를 보여주는 위치에 따라 보여줄 뉴스 개수 변경
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
            List<String> seenTitles = new ArrayList<>();

            JaccardSimilarity similarity = new JaccardSimilarity();
            double THRESHOLD = 0.6; // 이후 직접 조정

            int id = 0;

            for (NaverNewsDto.Item item : dto.getItems()) {

                if (filteredItems.size() >= 40) {
                    break;
                }

                // HTML 제거 + 디코딩
                String cleanTitle = item.getTitle().replaceAll("<[^>]*>", "");
                cleanTitle = StringEscapeUtils.unescapeHtml4(cleanTitle);

                String cleanDesc = item.getDescription().replaceAll("<[^>]*>", "");
                cleanDesc = StringEscapeUtils.unescapeHtml4(cleanDesc);

                // 정규화
                String normalizedTitle = normalizeTitle(cleanTitle);

                boolean isDuplicate = false;

                for (String seen : seenTitles) {
                    double score = similarity.apply(normalizedTitle, seen);
                    if (score >= THRESHOLD) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    continue;
                }

                // 새로운 뉴스로 확정
                seenTitles.add(normalizedTitle);

                item.setTitle(cleanTitle);
                item.setDescription(cleanDesc);
                item.setId(id++);

                filteredItems.add(item);
            }
            // (4) 원본 리스트를 중복 제거된 리스트로 교체
            dto.setItems(filteredItems);
        }

        return dto;
    }
    private String normalizeTitle(String title) {
        return title
                .toLowerCase()
                .replaceAll("[^a-z0-9가-힣\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

}
