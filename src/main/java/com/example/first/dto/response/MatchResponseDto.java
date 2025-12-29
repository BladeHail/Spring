package com.example.first.dto.response;

import com.example.first.entity.Match;
import com.example.first.entity.MatchResult;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponseDto {
    private Long id;

    //여러 팀이 동시에 경기할 경우를 고려해야 함
    private String teamA;
    private String teamB;
    private LocalDateTime matchDate;
    private String description;
    private boolean predictionOpen;
    private boolean alreadyPredicted;
    private MatchResult yourPrevResult;
    private long yourPrevBet;
    //그래프용 데이터
    private long homeAmount;
    private long awayAmount;

    public static MatchResponseDto fromEntity(Match match, boolean alreadyPredicted, MatchResult result,
                                              long bet, long homePercent, long awayPercent) {
        return MatchResponseDto.builder()
                .id(match.getId())
                .teamA(match.getTeamA())
                .teamB(match.getTeamB())
                .matchDate(match.getMatchDate())
                .description(match.getDescription())
                .predictionOpen(match.isPredictionOpen())
                .alreadyPredicted(alreadyPredicted)
                .yourPrevResult(result)
                .yourPrevBet(bet)
                .homeAmount(homePercent)
                .awayAmount(awayPercent)
                .build();
    }

}
