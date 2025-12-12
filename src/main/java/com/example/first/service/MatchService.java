package com.example.first.service;

import com.example.first.dto.MatchDto;
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
    public List<MatchDto> getPredictableMatches(Long userId) {
        List<Match> matches = matchRepository.findByPredictionOpenTrueOrderByMatchDateAsc();
        return convertToDtoList(matches, userId);
    }

    // 2. 전체 경기 목록 조회 - 관리자 페이지 또는 전체 목록
    public List<MatchDto> getAllMatches(Long userId) {
        List<Match> matches = matchRepository.findAllByOrderByMatchDateDesc();
        return convertToDtoList(matches, userId);
    }

    // 3. 특정 경기 단건 조회 (PredictionService에서 사용)
    public Match getMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));
    }

    // 4. 경기 생성 (관리자용)
    @Transactional
    public Match createMatch(Match match) {
        log.info("경기 생성: {} vs {}", match.getTeamA(), match.getTeamB());
        return matchRepository.save(match);
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

    // --- 내부 헬퍼 메서드 ---

    // [공통 로직] Entity 리스트 -> DTO 리스트 변환 (투표율 계산 포함)
    private List<MatchDto> convertToDtoList(List<Match> matches, Long userId) {
        return matches.stream().map(match -> {
            //1.DB에서 투표 수 집계
            long homeVotes = predictionRepository.countVotes(match.getId(), MatchResult.HOME_WIN);
            long awayVotes = predictionRepository.countVotes(match.getId(), MatchResult.AWAY_WIN);

            //2. 퍼센트 계산 (0으로 나누기 방지)
            int homePercent = calculatePercent(homeVotes, awayVotes);
            int awayPercent = (homeVotes + awayVotes == 0) ? 50 : (100 - homePercent);

            //3. 유저의 예측 정보 확인 (로그인한 경우)
            boolean alreadyPredicted = false;
            MatchResult myResult = MatchResult.NONE;

            if (userId != null) {
                Optional<Prediction> prediction = predictionRepository.findByUserIdAndMatch(userId, match);
                if (prediction.isPresent()) {
                    alreadyPredicted = true;
                    myResult = prediction.get().getPredictedResult();
                }
            }

            // 4.DTO 생성 (퍼센트 포함)
            return MatchDto.fromEntity(match, alreadyPredicted, myResult, homePercent, awayPercent);
        }).collect(Collectors.toList());
    }

    // [계산기] 퍼센트 계산 유틸
    private int calculatePercent(long homeVotes, long awayVotes) {
        long total = homeVotes + awayVotes;
        if (total == 0) {
            return 50; // 투표가 없으면 50:50으로 표시
        }
        // 정수 나눗셈 오차 방지를 위해 double 캐스팅
        return (int) ((double) homeVotes / total * 100);
    }
}

