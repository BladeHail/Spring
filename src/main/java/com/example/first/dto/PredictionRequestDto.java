package com.example.first.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionRequestDto {

    @NotNull(message = "경기를 선택해주세요")
    private Long matchId;

    @NotBlank(message = "예측 결과를 선택해주세요")
    private String predictedResult;
}
