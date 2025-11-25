package com.example.first.dto;

import com.example.first.entity.Match;
import com.example.first.entity.MatchResult;
import com.example.first.entity.Prediction;
import lombok.*;

import java.time.LocalDateTime;

import static com.example.first.entity.MatchResult.AWAY_WIN;
import static com.example.first.entity.MatchResult.HOME_WIN;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponseDto {
    private Long id;
    private Long matchId;
    private String teamA;
    private String teamB;
    private LocalDateTime matchDate;
    private String predictedResult;
    private String predictedResultText;
    private LocalDateTime predictedAt;
    private String actualResult;
    private String actualResultText;
    private Boolean isCorrect;
    private String matchStatus;

    public static PredictionResponseDto fromEntity(Prediction prediction) {
        Match match = prediction.getMatch();
        String predictedText = prediction.getResultText();

        String actualText = null;
        if (match.getActualResult() != null) {
            actualText = getResultText(
                    match.getActualResult(),
                    match.getTeamA(),
                    match.getTeamB()
            );
        }
        String status;
        if (match.getActualResult() != null) {
            status = "종료";
        } else if (match.getMatchDate().isBefore(LocalDateTime.now())) {
            status = "진행중";
        } else {
            status = "예정";
        }
        return PredictionResponseDto.builder()
                .id(prediction.getId())
                .matchId(match.getId())
                .teamA(match.getTeamA())
                .teamB(match.getTeamB())
                .matchDate(match.getMatchDate())
                .predictedResult(prediction.getPredictedResult().name())
                .predictedResultText(predictedText)
                .predictedAt(prediction.getPredictedAt())
                .actualResult(match.getActualResult() != null ?
                        match.getActualResult().name() : null)
                .actualResultText(actualText)
                .isCorrect(prediction.isCorrect())
                .matchStatus(status)
                .build();
    }
    private static String getResultText(MatchResult result, String teamA, String teamB) {
        switch (result) {
            case HOME_WIN:
                return teamA + " 승리";
            case AWAY_WIN:
                return teamB + " 승리";
            default:
                return "알 수 없음";
        }
    }
}