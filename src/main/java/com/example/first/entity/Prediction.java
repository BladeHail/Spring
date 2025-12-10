package com.example.first.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "predictions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "match_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchResult predictedResult;

    @Column(nullable = false)
    private LocalDateTime predictedAt;

    public Boolean isCorrect() {
        if (match.getActualResult() == null) {
            return null;
        }
        return predictedResult.equals(match.getActualResult());
    }

    public String getResultText() {
        return switch (predictedResult) {
            case HOME_WIN -> match.getTeamA() + " 승리";
            case AWAY_WIN -> match.getTeamB() + " 승리";
            default -> "알 수 없음";
        };
    }
}

