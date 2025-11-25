package com.example.first.service;

import com.example.first.entity.Match;
import com.example.first.entity.MatchResult;
import com.example.first.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {
    private final MatchRepository matchRepository;

    public List<Match> getPredictableMatches() {
        return matchRepository.findByPredictionOpenTrueOrderByMatchDateAsc();
    }
    public List<Match> getAllMatches() {
        return matchRepository.findAllByOrderByMatchDateDesc();
    }
    public Match getMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));
    }
    @Transactional
    public Match createMatch(Match match) {
        log.info("경기 생성: {} vs {}", match.getTeamA(), match.getTeamB());
        return matchRepository.save(match);
    }
    @Transactional
    public Match updateMatchResult(Long matchId, MatchResult result) {
        Match match = getMatchById(matchId);
        match.setActualResult(result);
        match.setPredictionOpen(false);
        log.info("경기 결과 입력: {} vs {} -> {}",
                match.getTeamA(), match.getTeamB(), result);
        return matchRepository.save(match);
    }
}

