package com.example.first.service;

import com.example.first.client.YoutubeVideoClient;
import com.example.first.dto.YoutubeVideoDetail;
import com.example.first.entity.YoutubeVideo;
import com.example.first.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoAdminService {

    private final VideoRepository videoRepository;
    private final YoutubeVideoClient videoClient; // 새로운 링크 정보를 가져오기 위해 필요

    @Transactional
    public void updateVideo(Long id, String youtubeUrl, String keyword) {
        // 1. 기존 영상 데이터 조회
        YoutubeVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 영상을 찾을 수 없습니다. id=" + id));

        // 2. 링크(URL)가 새로 입력되었는지 확인
        if (youtubeUrl != null && !youtubeUrl.isBlank()) {
            // 새로운 링크의 정보를 유튜브 API에서 가져옴
            YoutubeVideoDetail detail = videoClient.fetchByUrl(youtubeUrl);

            // 엔티티의 모든 필드를 새 정보로 교체
            video.updateFullInfo(
                    detail.videoId(),
                    detail.title(),
                    detail.thumbnailUrl(),
                    detail.channelId(),
                    detail.channelTitle(),
                    detail.publishedAt(),
                    keyword
            );
        } else {
            // 3. 링크 입력이 없다면 기존 영상의 키워드만 수정
            video.updateKeyword(keyword);
        }

    }

    @Transactional
    public void deleteVideo(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 영상이 존재하지 않습니다. id=" + id);
        }
        videoRepository.deleteById(id);
    }
}