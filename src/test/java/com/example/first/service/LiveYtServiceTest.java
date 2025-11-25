package com.example.first.service;

import com.example.first.dto.LiveYtDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LiveYtServiceTest {
    @Autowired
    private LiveYtService liveYtService;

    @Test
    void youtubeApiTest(){
        System.out.println("=== 🔴 유튜브 API 테스트 시작 ===");

        try{
            liveYtService.checkYtLiveStatus();
            List<LiveYtDto> result=liveYtService.getLiveVideos();

            System.out.println("=== 🟢 검색 결과 확인 ===");
            if(result.isEmpty()){
                System.out.println("현재 라이브 중인 방송이 없습니다. (정상)");
            } else {
                for(LiveYtDto video:result){
                    System.out.println("채널 ID: " + video.getChannelId());
                    System.out.println("제목: " + video.getTitle());
                    System.out.println("영상 ID: " + video.getVideo());
                    System.out.println("-------------------------");
                }
            }
            assertThat(result).isNotNull();
        } catch (Exception e) {
            System.out.println("=== 🚨 에러 발생 ===");
            e.printStackTrace();
        }
    }
}
