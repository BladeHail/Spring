package com.example.first.service;

import com.example.first.dto.NaverNewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class NaverNewsService {

    private final RestTemplate restTemplate;

    //프로퍼티스에서 키 가져오기
    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.url.search.news}")
    private String naverUrl;

    public NaverNewsDto searchNews(String keyword){
        // 1. URI 생성 (쿼리 파라미터 인코딩 처리)
        URI uri = UriComponentsBuilder.fromHttpUrl(naverUrl)
                .queryParam("query", keyword)
                .queryParam("display", 3) //뉴스를 보여주는 위치에 따라 보여줄 뉴스 개수 변경
                .queryParam("sort", "date")//최신순 정렬
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
        ResponseEntity<NaverNewsDto> response = restTemplate.exchange(req, NaverNewsDto.class);

        return response.getBody();
    }
}
