package com.example.first.controller;

import com.example.first.dto.PlayerRequestDto;
import com.example.first.dto.PlayerResponseDto;
import com.example.first.entity.PlayerEntity;
import com.example.first.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    @GetMapping
    public List<PlayerResponseDto> allPlayers() {
        return playerService.getAllPlayers();
    }
    @GetMapping("/{id}")
    public PlayerResponseDto getPlayer(@PathVariable Long id) {
        PlayerEntity e = playerService.getPlayEntity(id);
        return PlayerResponseDto.from(e);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> addPlayer(
            @RequestPart("player") PlayerRequestDto dto,
            @RequestPart(value = "media", required = false) MultipartFile file
    ) {
        Long id = playerService.createPlayer(dto, file);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }
}
