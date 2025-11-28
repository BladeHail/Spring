package com.example.first.service;

import com.example.first.dto.PredictionRequestDto;
import com.example.first.dto.PredictionResponseDto;
import com.example.first.entity.*;
import com.example.first.repository.PredictionRepository;
import com.example.first.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PredictionService 테스트")
class PredictionServiceTest {

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private MatchService matchService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PredictionService predictionService;

    private User user;
    private Match match;
    private PredictionRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .password("password")
                .provider(AuthProvider.LOCAL)
                .build();

        match = Match.builder()
                .id(1L)
                .teamA("한국")
                .teamB("일본")
                .matchDate(LocalDateTime.now().plusDays(1))
                .predictionOpen(true)
                .build();

        requestDto = PredictionRequestDto.builder()
                .matchId(1L)
                .predictedResult("HOME_WIN")
                .build();
    }

    @Test
    @DisplayName("예측 생성 성공")
    void createPredictionSuccess() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(matchService.getMatchById(1L)).thenReturn(match);
        when(predictionRepository.existsByUserIdAndMatch(1L, match)).thenReturn(false);

        Prediction savedPrediction = Prediction.builder()
                .id(1L)
                .user(user)
                .match(match)
                .predictedResult(MatchResult.HOME_WIN)
                .predictedAt(LocalDateTime.now())
                .build();
        when(predictionRepository.save(any(Prediction.class))).thenReturn(savedPrediction);

        // when
        Prediction result = predictionService.createPrediction(1L, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPredictedResult()).isEqualTo(MatchResult.HOME_WIN);

        verify(userRepository).findById(1L);
        verify(matchService).getMatchById(1L);
        verify(predictionRepository).existsByUserIdAndMatch(1L, match);
        verify(predictionRepository).save(any(Prediction.class));
    }

    @Test
    @DisplayName("예측 생성 실패 - 존재하지 않는 사용자")
    void createPredictionFail_UserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> predictionService.createPrediction(1L, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");

        verify(userRepository).findById(1L);
        verify(predictionRepository, never()).save(any(Prediction.class));
    }

    @Test
    @DisplayName("예측 생성 실패 - 이미 예측한 경기")
    void createPredictionFail_AlreadyPredicted() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(matchService.getMatchById(1L)).thenReturn(match);
        when(predictionRepository.existsByUserIdAndMatch(1L, match)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> predictionService.createPrediction(1L, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 예측한 경기입니다.");

        verify(predictionRepository, never()).save(any(Prediction.class));
    }

    @Test
    @DisplayName("예측 생성 실패 - 예측 마감된 경기")
    void createPredictionFail_PredictionClosed() {
        // given
        match.setPredictionOpen(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(matchService.getMatchById(1L)).thenReturn(match);

        // when & then
        assertThatThrownBy(() -> predictionService.createPrediction(1L, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("예측이 마감된 경기입니다.");

        verify(predictionRepository, never()).save(any(Prediction.class));
    }

    @Test
    @DisplayName("예측 생성 실패 - 경기 이미 시작")
    void createPredictionFail_MatchAlreadyStarted() {
        // given
        Match pastMatch = Match.builder()
                .id(1L)
                .teamA("한국")
                .teamB("일본")
                .matchDate(LocalDateTime.now().minusHours(1)) // 과거 시간
                .predictionOpen(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(matchService.getMatchById(1L)).thenReturn(pastMatch);
        when(predictionRepository.existsByUserIdAndMatch(1L, pastMatch)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> predictionService.createPrediction(1L, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("경기가 이미 시작되었습니다.");

        verify(predictionRepository, never()).save(any(Prediction.class));
    }

    @Test
    @DisplayName("사용자 예측 목록 조회")
    void getUserPredictions() {
        // given
        Prediction prediction1 = Prediction.builder()
                .id(1L)
                .user(user)
                .match(match)
                .predictedResult(MatchResult.HOME_WIN)
                .predictedAt(LocalDateTime.now())
                .build();

        when(predictionRepository.findByUserIdOrderByPredictedAtDesc(1L))
                .thenReturn(Arrays.asList(prediction1));

        // when
        List<PredictionResponseDto> results = predictionService.getUserPredictions(1L);

        // then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPredictedResult()).isEqualTo("HOME_WIN");

        verify(predictionRepository).findByUserIdOrderByPredictedAtDesc(1L);
    }

    @Test
    @DisplayName("사용자 예측 여부 확인 - 예측함")
    void hasUserPredicted_True() {
        // given
        when(matchService.getMatchById(1L)).thenReturn(match);
        when(predictionRepository.existsByUserIdAndMatch(1L, match)).thenReturn(true);

        // when
        boolean result = predictionService.hasUserPredicted(1L, 1L);

        // then
        assertThat(result).isTrue();

        verify(matchService).getMatchById(1L);
        verify(predictionRepository).existsByUserIdAndMatch(1L, match);
    }

    @Test
    @DisplayName("사용자 예측 여부 확인 - 예측 안함")
    void hasUserPredicted_False() {
        // given
        when(matchService.getMatchById(1L)).thenReturn(match);
        when(predictionRepository.existsByUserIdAndMatch(1L, match)).thenReturn(false);

        // when
        boolean result = predictionService.hasUserPredicted(1L, 1L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자 통계 조회")
    void getUserStats() {
        // given
        Match completedMatch = Match.builder()
                .id(1L)
                .teamA("한국")
                .teamB("일본")
                .matchDate(LocalDateTime.now().minusDays(1))
                .actualResult(MatchResult.HOME_WIN)
                .predictionOpen(false)
                .build();

        Prediction correctPrediction = Prediction.builder()
                .id(1L)
                .user(user)
                .match(completedMatch)
                .predictedResult(MatchResult.HOME_WIN)
                .predictedAt(LocalDateTime.now().minusDays(2))
                .build();

        when(predictionRepository.countByUserId(1L)).thenReturn(5L);
        when(predictionRepository.countByUserIdAndMatch_ActualResultNotNull(1L)).thenReturn(3L);
        when(predictionRepository.findByUserIdOrderByPredictedAtDesc(1L))
                .thenReturn(Arrays.asList(correctPrediction));

        // when
        PredictionService.PredictionStats stats = predictionService.getUserStats(1L);

        // then
        assertThat(stats.getTotalPredictions()).isEqualTo(5L);
        assertThat(stats.getCompletedMatches()).isEqualTo(3L);
        assertThat(stats.getCorrectPredictions()).isEqualTo(1L);
        assertThat(stats.getAccuracy()).isGreaterThan(0.0);
    }
}