package com.example.first.dto;

import com.example.first.entity.PlayerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PlayerDto {
    private Long id;
    private String name;
    private String body;
    private String type;
    private String team;
    private String media;

    public static PlayerDto from(PlayerEntity e) {
        return PlayerDto.builder()
                .id(e.getId())
                .name(e.getName())
                .body(e.getBody())
                .type(e.getType())
                .team(e.getTeam())
                .media(e.getMedia())
                .build();
    }
}
