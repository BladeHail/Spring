package com.example.first.controller;

import com.example.first.dto.MatchDto;
import com.example.first.dto.PredictionRequestDto;
import com.example.first.dto.PredictionResponseDto;
import com.example.first.entity.Match;
import com.example.first.service.MatchService;
import com.example.first.service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/predictions")
public class PredictionController {

    private final MatchService matchService;
    private final PredictionService predictionService;

    @GetMapping("/matches")
    public String matchList(Model model, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        List<Match> matches = matchService.getPredictableMatches();

        List<MatchDto> matchDtos = matches.stream()
                .map(match -> MatchDto.fromEntity(
                        match,
                        predictionService.hasUserPredicted(userId, match.getId())
                ))
                .collect(Collectors.toList());

        model.addAttribute("matches", matchDtos);
        return "predictions/match-list";
    }

    /**
     * 예측 입력 페이지
     */
    @GetMapping("/predict/{matchId}")
    public String predictForm(@PathVariable Long matchId,
                              Model model,
                              Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        Match match = matchService.getMatchById(matchId);

        if (predictionService.hasUserPredicted(userId, matchId)) {
            return "redirect:/predictions/matches?error=already";
        }

        if (!match.isPredictionOpen()) {
            return "redirect:/predictions/matches?error=closed";
        }

        model.addAttribute("match", match);
        model.addAttribute("predictionRequest", new PredictionRequestDto());
        return "predictions/predict-form";
    }

    @PostMapping("/predict")
    public String submitPrediction(@Valid @ModelAttribute("predictionRequest") PredictionRequestDto requestDto,
                                   BindingResult bindingResult,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "redirect:/predictions/predict/" + requestDto.getMatchId() + "?error=validation";
        }

        try {
            Long userId = getCurrentUserId(authentication);
            predictionService.createPrediction(userId, requestDto);

            redirectAttributes.addFlashAttribute("message", "예측이 성공적으로 등록되었습니다!");
            return "redirect:/predictions/my-predictions";

        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("예측 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/predictions/matches";
        }
    }

    @GetMapping("/my-predictions")
    public String myPredictions(Model model, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        // 내 예측 목록
        List<PredictionResponseDto> predictions = predictionService.getUserPredictions(userId);

        // 통계 정보
        PredictionService.PredictionStats stats = predictionService.getUserStats(userId);

        model.addAttribute("predictions", predictions);
        model.addAttribute("stats", stats);
        return "predictions/my-predictions";
    }

    private Long getCurrentUserId(Authentication authentication) {

        return 1L;
    }
}