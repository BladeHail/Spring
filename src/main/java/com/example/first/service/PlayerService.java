package com.example.first.service;

import com.example.first.dto.PlayerRequestDto;
import com.example.first.dto.PlayerResponseDto;
import com.example.first.entity.PlayerEntity;
import com.example.first.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<PlayerEntity> searchPlayers(String name, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // 검색어 없으면 전체 조회
        if (name == null || name.isBlank()) {
            return repository.findAll(pageable);
        }

        // 검색어 있으면 이름 기준 검색 + 페이징
        return repository.findByNameContainingIgnoreCase(name, pageable);
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

