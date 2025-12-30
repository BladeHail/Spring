package com.example.first.controller;

import com.example.first.dto.request.PredictionRequestDto;
import com.example.first.dto.response.PredictionResponseDto;
import com.example.first.security.oauth2.PrincipalDetails;
import com.example.first.service.MatchService;
import com.example.first.service.PredictionService;
import com.example.first.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final UserService userService;

    @GetMapping("/matches")
    public ResponseEntity<?> getMatches(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        //서비스 메서드만 호출
        return new ResponseEntity<>(matchService.getPredictableMatches(userId), HttpStatus.OK);
    }
    @GetMapping("/matches/admin")
    public ResponseEntity<?> getAllMatches(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        //서비스 메서드만 호출
        return new ResponseEntity<>(matchService.getAllMatches(userId), HttpStatus.OK);
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
    public ResponseEntity<?> createPrediction(
            @RequestBody PredictionRequestDto dto,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        if(userId == null) {
            return new ResponseEntity<>("Unauthenticated User", HttpStatus.BAD_REQUEST);
        }
        return predictionService.createPrediction(userId, dto);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Wrong authentication");
            return null;
        }
        Object principal = authentication.getPrincipal();

        if (principal instanceof PrincipalDetails principalDetails) {
            return principalDetails.getUser().getId();
        }
        System.out.println("지원하지 않는 인증 방식이거나 사용자 정보를 찾을수 없습니다.");
        return null;
    }
}

