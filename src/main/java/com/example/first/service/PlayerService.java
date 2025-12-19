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
    public Long fixPlayer(PlayerRequestDto dto, MultipartFile file) {
        // 1) 파일 저장
        String mediaPath;
        if(repository.findById(dto.getId()).isPresent()) {
            mediaPath = repository.findById(dto.getId()).get().getMedia();
        }
        else {
            System.out.println("No such player...");
            return null;
        }
        if (file != null && !file.isEmpty()) {
            mediaPath = fileStorage.save(file);
            //Should remove old file
        }
        // 2) 엔티티 생성
        PlayerEntity entity = repository.findById(dto.getId()).get();
        entity.setMedia(mediaPath);
        entity.setName(dto.getName());
        entity.setBody(dto.getBody());
        entity.setType(dto.getType());
        entity.setTeam(dto.getTeam());
        entity.setAwards(dto.getAwards());
        /*PlayerEntity entity = PlayerEntity.builder()
                .name(dto.getName())
                .body(dto.getBody())
                .type(dto.getType())
                .team(dto.getTeam())
                .media(mediaPath)
                .awards(dto.getAwards())
                .build();*/
        // 3) 저장
        PlayerEntity saved = repository.save(entity);
        // 4) 저장된 ID 반환
        return saved.getId();
    }

    public Long deletePlayer(Long id) {
        PlayerEntity saved = repository.findById(id).orElse(null);
        if(saved != null) {
            saved.setDeleted(true);
            repository.save(saved);
            return saved.getId();
        }
        else return null;
    }
}

