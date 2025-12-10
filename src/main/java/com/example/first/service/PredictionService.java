package com.example.first.service;

import com.example.first.dto.PredictionRequestDto;
import com.example.first.dto.PredictionResponseDto;
import com.example.first.entity.Match;
import com.example.first.entity.MatchResult;
import com.example.first.entity.Prediction;
import com.example.first.entity.User;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchService matchService;
    private final UserRepository userRepository;

    @Transactional
    public Prediction createPrediction(Long userId, PredictionRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Match match = matchService.getMatchById(requestDto.getMatchId());

        // 1. 관리자가 수동으로 마감했는지 체크
        if (!match.isPredictionOpen()) {
            throw new IllegalStateException("예측이 마감된 경기입니다.");
        }

        // 2. 경기 시작 10분 전 마감 체크
        // 경기 시간 10분 전 시간 구하기
        LocalDateTime deadline = match.getMatchDate().minusMinutes(10);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new IllegalStateException("경기 시작 10분 전까지만 예측(수정)할 수 있습니다.");
        }

        // 3. 올바른 결과값인지 검증
        if (!isValidResult(requestDto.getPredictedResult())) {
            throw new IllegalStateException("올바르지 않은 예측 결과입니다.");
        }

        MatchResult newResult = MatchResult.valueOf(requestDto.getPredictedResult());

        // 4. 기존 예측 확인 (있으면 수정, 없으면 생성)
        Optional<Prediction> existingPrediction = predictionRepository.findByUserIdAndMatch(userId, match);

        Prediction prediction;
        if (existingPrediction.isPresent()) {
            prediction = existingPrediction.get();
            prediction.setPredictedResult(newResult);
            prediction.setPredictedAt(LocalDateTime.now()); // 수정 시간 업데이트
            log.info("예측 수정: userId={}, matchId={}, result={}", userId, match.getId(), newResult);
        } else {
            prediction = Prediction.builder()
                    .user(user)
                    .match(match)
                    .predictedResult(newResult)
                    .predictedAt(LocalDateTime.now())
                    .build();
            log.info("예측 생성: userId={}, matchId={}, result={}", userId, match.getId(), newResult);
        }

        return predictionRepository.save(prediction);
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
    public MatchResult getPreviousPrediction(Long userId, Long matchId) {
        Match match = matchService.getMatchById(matchId);
        Optional<Prediction> existingPrediction = predictionRepository.findByUserIdAndMatch(userId, match);
        if(existingPrediction.isPresent()) {
            return existingPrediction.get().getPredictedResult();
        }
        else return MatchResult.NONE;
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