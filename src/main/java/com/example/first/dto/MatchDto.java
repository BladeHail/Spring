package com.example.first.dto;

import com.example.first.entity.Match;
import com.example.first.entity.MatchResult;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDto {
    private Long id;

    //여러 팀이 동시에 경기할 경우를 고려해야 함
    private String teamA;
    private String teamB;
    private LocalDateTime matchDate;
    private String description;
    private boolean predictionOpen;
    private boolean alreadyPredicted;
    private MatchResult yourPrevResult;
    //그래프용 데이터
    private int homePercent;
    private int awayPercent;

    public static MatchDto fromEntity(Match match, boolean alreadyPredicted, MatchResult result,
                                      int homePercent, int awayPercent) {
        return MatchDto.builder()
                .id(match.getId())
                .teamA(match.getTeamA())
                .teamB(match.getTeamB())
                .matchDate(match.getMatchDate())
                .description(match.getDescription())
                .predictionOpen(match.isPredictionOpen())
                .alreadyPredicted(alreadyPredicted)
                .yourPrevResult(result)
                .homePercent(homePercent)
                .awayPercent(awayPercent)
                .build();
    }

}
