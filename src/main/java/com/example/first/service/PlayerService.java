package com.example.first.service;

import com.example.first.dto.PlayerRequestDto;
import com.example.first.dto.PlayerResponseDto;
import com.example.first.entity.PlayerEntity;
import com.example.first.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository repository;
    private final FileStorageService fileStorage;

    public PlayerService(PlayerRepository repository, FileStorageService fileStorage) {
        this.repository = repository;
        this.fileStorage = fileStorage;
    }

    public List<PlayerResponseDto> getAllPlayers() {
        List<PlayerEntity> list = repository.findAll();
        List<PlayerResponseDto> players = new ArrayList<>();
        list.forEach(e -> {
            PlayerResponseDto dto = PlayerResponseDto.from(e);
            players.add(dto);
        });
        return players;
    }

    public PlayerEntity getPlayEntity(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Long createPlayer(PlayerRequestDto dto, MultipartFile file) {
        // 1) 파일 저장
        String mediaPath = null;
        if (file != null && !file.isEmpty()) {
            mediaPath = fileStorage.save(file);
        }
        // 2) 엔티티 생성
        PlayerEntity entity = PlayerEntity.builder()
                .name(dto.getName())
                .body(dto.getBody())
                .type(dto.getType())
                .team(dto.getTeam())
                .media(mediaPath)
                .awards(dto.getAwards())
                .build();
        // 3) 저장
        PlayerEntity saved = repository.save(entity);
        // 4) 저장된 ID 반환
        return saved.getId();
    }
}

