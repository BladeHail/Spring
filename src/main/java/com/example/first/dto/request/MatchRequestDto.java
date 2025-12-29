package com.example.first.dto.request;

import com.example.first.entity.MatchResult;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequestDto {
    private Long id;
    private boolean predictionOpen;
    private MatchResult result;
    private LocalDateTime matchDate;
    private String teamA;
    private String teamB;
    private String description;
}
