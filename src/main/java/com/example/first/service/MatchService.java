package com.example.first.service;

import com.example.first.dto.request.MatchRequestDto;
import com.example.first.dto.response.MatchResponseDto;
import com.example.first.entity.Match;
import com.example.first.entity.MatchResult;
import com.example.first.entity.Prediction;
import com.example.first.repository.MatchRepository;
import com.example.first.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {
    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository; // [필수] 투표 집계용

    // 1. 경기 목록 조회 (예측 가능한 경기들) - 메인 화면 등
    public List<MatchResponseDto> getPredictableMatches(Long userId) {
        List<Match> matches = matchRepository.findByPredictionOpenTrueOrderByIdDesc();
        return convertToDtoList(matches, userId);
    }

    // 2. 전체 경기 목록 조회 - 관리자 페이지 또는 전체 목록
    public List<MatchResponseDto> getAllMatches(Long userId) {
        List<Match> matches = matchRepository.findAllByOrderByIdDesc();
        return convertToDtoList(matches, userId);
    }

    // 3. 특정 경기 단건 조회 (PredictionService에서 사용)
    public Match getMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));
    }

    // 4. 경기 생성 (관리자용)
    @Transactional
    public void createMatch(MatchRequestDto dto) {
        Match match = new Match();
        if(dto.getTeamA().isEmpty()) dto.setTeamA("Home");
        if(dto.getTeamB().isEmpty()) dto.setTeamB("Away");
        match.setTeamA(dto.getTeamA());
        match.setTeamB(dto.getTeamB());
        match.setMatchDate(dto.getMatchDate());
        if(!dto.getDescription().isEmpty()) match.setDescription(dto.getDescription());
        match.setPredictionOpen(true);
        matchRepository.save(match);
        log.info("경기 생성: {} vs {}", dto.getTeamA(), dto.getTeamB());
    }

    // 5. 경기 결과 입력 및 마감 (관리자용)
    @Transactional
    public Match updateMatchResult(Long matchId, MatchResult result) {
        Match match = getMatchById(matchId);
        match.setActualResult(result);
        match.setPredictionOpen(false); // 결과 나오면 투표 마감
        log.info("경기 결과 입력: {} vs {} -> {}", match.getTeamA(), match.getTeamB(), result);
        return matchRepository.save(match);
    }

    @Transactional
    public void updateMatch(MatchRequestDto dto) {
        Match match = getMatchById(dto.getId());
        match.setActualResult(dto.getResult());
        match.setMatchDate(dto.getMatchDate());
        match.setPredictionOpen(dto.isPredictionOpen());
        log.info("경기 결과 입력: {} {} {}", dto.getMatchDate(), dto.getResult(), dto.isPredictionOpen());
        matchRepository.save(match);
    }

    // --- 내부 헬퍼 메서드 ---

    // [공통 로직] Entity 리스트 -> DTO 리스트 변환 (투표율 계산 포함)
    private List<MatchResponseDto> convertToDtoList(List<Match> matches, Long userId) {
        return matches.stream().map(match -> {
            //1.DB에서 투표 수 집계
            long homePercent = predictionRepository.countVotes(match.getId(), MatchResult.HOME_WIN);
            long awayPercent = predictionRepository.countVotes(match.getId(), MatchResult.AWAY_WIN);

            //2. 유저의 예측 정보 확인 (로그인한 경우)
            boolean alreadyPredicted = false;
            long bet = 1;
            MatchResult myResult = MatchResult.NONE;

            if (userId != null) {
                Optional<Prediction> prediction = predictionRepository.findByUserIdAndMatch(userId, match);
                if (prediction.isPresent()) {
                    alreadyPredicted = true;
                    bet = prediction.get().getBet();
                    myResult = prediction.get().getPredictedResult();
                }
            }

            // 3.DTO 생성 (퍼센트 포함)
            return MatchResponseDto.fromEntity(match, alreadyPredicted, myResult, bet, homePercent, awayPercent);
        }).collect(Collectors.toList());
    }
}

