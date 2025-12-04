package com.example.first.dto;

import com.example.first.entity.PlayerEntity;
import lombok.Getter;

import java.util.List;

@Getter
public class PlayerResponseDto {
    private Long id;
    private String name;
    private String body;
    private String type;
    private String team;
    private String mediaUrl;
    private List<String> awards;

    public static PlayerResponseDto from(PlayerEntity e) {
        PlayerResponseDto dto = new PlayerResponseDto();
        dto.id = e.getId();
        dto.name = e.getName();
        dto.body = e.getBody();
        dto.type = e.getType();
        dto.team = e.getTeam();
        dto.mediaUrl = e.getMedia();
        dto.awards = e.getAwards();
        return dto;
    }
}

