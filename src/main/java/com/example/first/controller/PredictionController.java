package com.example.first.controller;

import com.example.first.dto.MatchDto;
import com.example.first.dto.PredictionRequestDto;
import com.example.first.dto.PredictionResponseDto;
import com.example.first.entity.Match;
import com.example.first.security.oauth2.PrincipalDetails;
import com.example.first.service.MatchService;
import com.example.first.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final MatchService matchService;
    private final PredictionService predictionService;

    @GetMapping("/matches")
    public List<MatchDto> getMatches(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        //서비스 메서드만 호출
        return matchService.getPredictableMatches(userId);
    }

    // 2. 내 예측 내역
    @GetMapping("/my")
    public List<PredictionResponseDto> myPredictions(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        //서비스가 퍼센트로 계산해서 DTO로 보냄.
        return predictionService.getUserPredictions(userId);
    }
    // 3. 통계
    @GetMapping("/stats")
    public PredictionService.PredictionStats stats(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        return predictionService.getUserStats(userId);
    }
    // 4. 예측 투표하기
    @PostMapping
    public PredictionResponseDto createPrediction(
            @RequestBody PredictionRequestDto dto,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        return predictionService.createPrediction(userId, dto);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("로그인이 필요한 기능입니다.");
        }
        Object principal = authentication.getPrincipal();

        if (principal instanceof PrincipalDetails) {
            PrincipalDetails principalDetails = (PrincipalDetails) principal;
            return principalDetails.getUser().getId();
        }
        throw new IllegalStateException("지원하지 않는 인증 방식이거나 사용자 정보를 찾을수 없습니다.");
    }
}

