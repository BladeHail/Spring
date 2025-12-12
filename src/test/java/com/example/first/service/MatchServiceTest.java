package com.example.first.service;

import com.example.first.dto.MatchDto;
import com.example.first.entity.Match;
import com.example.first.entity.MatchResult;
import com.example.first.repository.MatchRepository;
import com.example.first.repository.PredictionRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService 테스트")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PredictionRepository predictionRepository;

    @InjectMocks
    private MatchService matchService;

    private Match match;

    @BeforeEach
    void setUp() {
        match = Match.builder()
                .id(1L)
                .teamA("한국")
                .teamB("일본")
                .matchDate(LocalDateTime.now().plusDays(1))
                .predictionOpen(true)
                .description("2024 올림픽 준결승")
                .build();
    }

    @Test
    @DisplayName("예측 가능한 경기 목록 조회")
    void getPredictableMatches() {
        // given
        List<Match> matches = Arrays.asList(match);
        when(matchRepository.findByPredictionOpenTrueOrderByMatchDateAsc()).thenReturn(matches);
        given(predictionRepository.countVotes(any(), any())).willReturn(0L);

        // when
        List<MatchDto> result = matchService.getPredictableMatches(null);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isPredictionOpen()).isTrue();
        assertThat(result.get(0).getTeamA()).isEqualTo("한국");

        verify(matchRepository).findByPredictionOpenTrueOrderByMatchDateAsc();
    }

    @Test
    @DisplayName("전체 경기 목록 조회")
    void getAllMatches() {
        // given
        List<Match> matches = Arrays.asList(match);
        when(matchRepository.findAllByOrderByMatchDateDesc()).thenReturn(matches);
        given(predictionRepository.countVotes(any(), any())).willReturn(0L);

        // when
        List<MatchDto> result = matchService.getAllMatches(null);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeamA()).isEqualTo("한국"); // 팀 이름 확인
        assertThat(result.get(0).getHomePercent()).isEqualTo(50);

        verify(matchRepository).findAllByOrderByMatchDateDesc();
    }

    @Test
    @DisplayName("ID로 경기 조회 - 성공")
    void getMatchByIdSuccess() {
        // given
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        // when
        Match result = matchService.getMatchById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTeamA()).isEqualTo("한국");
        assertThat(result.getTeamB()).isEqualTo("일본");

        verify(matchRepository).findById(1L);
    }

    @Test
    @DisplayName("ID로 경기 조회 - 실패 (존재하지 않는 경기)")
    void getMatchByIdFail_NotFound() {
        // given
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> matchService.getMatchById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 경기입니다.");

        verify(matchRepository).findById(999L);
    }

    @Test
    @DisplayName("경기 생성")
    void createMatch() {
        // given
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        // when
        Match result = matchService.createMatch(match);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTeamA()).isEqualTo("한국");
        assertThat(result.getTeamB()).isEqualTo("일본");

        verify(matchRepository).save(any(Match.class));
    }

    @Test
    @DisplayName("경기 결과 업데이트")
    void updateMatchResult() {
        // given
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        // when
        Match result = matchService.updateMatchResult(1L, MatchResult.HOME_WIN);

        // then
        assertThat(result.getActualResult()).isEqualTo(MatchResult.HOME_WIN);
        assertThat(result.isPredictionOpen()).isFalse();

        verify(matchRepository).findById(1L);
        verify(matchRepository).save(any(Match.class));
    }
    @Test
    @DisplayName("승부예측 그래프 비율 계산 테스트")
    void testGraphPercentages() {
        // 테스트용 경기 객체 하나 생성
        List<Match> matches = Arrays.asList(match);

        // 경기 목록 가져오기
        given(matchRepository.findByPredictionOpenTrueOrderByMatchDateAsc()).willReturn(matches);

        //가짜 투표 데이터
        // 7
        given(predictionRepository.countVotes(any(), eq(MatchResult.HOME_WIN))).willReturn(7L);
        // 3
        given(predictionRepository.countVotes(any(), eq(MatchResult.AWAY_WIN))).willReturn(3L);

        // 2.실행
        List<MatchDto> result = matchService.getPredictableMatches(null);

        // 3.검증
        MatchDto dto = result.get(0);

        // 전체 10표 중 7표니까 70
        assertThat(dto.getHomePercent()).isEqualTo(70);

        // 전체 10표 중 3표니까 30
        assertThat(dto.getAwayPercent()).isEqualTo(30);

        System.out.println("A팀 비율: " + dto.getHomePercent() + "%"); // 70%
        System.out.println("B팀 비율: " + dto.getAwayPercent() + "%"); // 30%
    }
}