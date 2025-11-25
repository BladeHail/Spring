package com.example.first.service;

import com.example.first.dto.PredictionRequestDto;
import com.example.first.dto.PredictionResponseDto;
import com.example.first.entity.Match;
import com.example.first.entity.MatchResult;
import com.example.first.entity.Prediction;
import com.example.first.entity.User;
import com.example.first.repository.MatchRepository;
import com.example.first.repository.PredictionRepository;
import com.example.first.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchService matchService;
    private final UserRepository userRepository;  //


    @Transactional
    public Prediction createPrediction(Long userId, PredictionRequestDto requestDto) {
        // User 조회 추가
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Match match = matchService.getMatchById(requestDto.getMatchId());

        // 검증
        if (!match.isPredictionOpen()) {
            throw new IllegalStateException("예측이 마감된 경기입니다.");
        }
        if (predictionRepository.existsByUserIdAndMatch(userId, match)) {
            throw new IllegalStateException("이미 예측한 경기입니다.");
        }
        if (LocalDateTime.now().isAfter(match.getMatchDate())) {
            throw new IllegalStateException("경기가 이미 시작되었습니다.");
        }
        if (!isValidResult(requestDto.getPredictedResult())) {
            throw new IllegalStateException("올바르지 않은 예측 결과입니다.");
        }

        Prediction prediction = Prediction.builder()
                .user(user)
                .match(match)
                .predictedResult(MatchResult.valueOf(requestDto.getPredictedResult()))
                .predictedAt(LocalDateTime.now())
                .build();

        Prediction saved = predictionRepository.save(prediction);
        log.info("예측 생성: userId={}, matchId={}, result={}",
                userId, match.getId(), requestDto.getPredictedResult());
        return saved;
    }

    public List<PredictionResponseDto> getUserPredictions(Long userId) {
        List<Prediction> predictions = predictionRepository.findByUserIdOrderByPredictedAtDesc(userId);
        return predictions.stream()
                .map(PredictionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public boolean hasUserPredicted(Long userId, Long matchId) {
        Match match = matchService.getMatchById(matchId);
        return predictionRepository.existsByUserIdAndMatch(userId, match);
    }

    public PredictionStats getUserStats(Long userId) {
        long totalPredictions = predictionRepository.countByUserId(userId);
        long completedMatches = predictionRepository.countByUserIdAndMatch_ActualResultNotNull(userId);

        List<Prediction> allPredictions = predictionRepository.findByUserIdOrderByPredictedAtDesc(userId);
        long correctPredictions = allPredictions.stream()
                .filter(p -> Boolean.TRUE.equals(p.isCorrect()))
                .count();

        return new PredictionStats(totalPredictions, completedMatches, correctPredictions);
    }

    private boolean isValidResult(String result) {
        try {
            MatchResult.valueOf(result);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class PredictionStats {
        private long totalPredictions;
        private long completedMatches;
        private long correctPredictions;

        public double getAccuracy() {
            if (completedMatches == 0) return 0.0;
            return (double) correctPredictions / completedMatches * 100;
        }
    }
}