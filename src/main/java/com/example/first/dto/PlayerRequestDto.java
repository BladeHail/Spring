package com.example.first.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRequestDto {
    private String name; // 이름

    private String body; // 신체정보 예: "175cm 64kg"

    private String type; // 종목 예: "500m, 1000m, 1500m"

    private String team;

    private List<String> awards;
}
