package com.example.first.controller;

import com.example.first.dto.PlayerDto;
import com.example.first.dto.PlayerRequestDto;
import com.example.first.dto.PlayerResponseDto;
import com.example.first.entity.PlayerEntity;
import com.example.first.entity.User;
import com.example.first.service.AuthService;
import com.example.first.service.PlayerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerService playerService;
    private final AuthService authService;

    public PlayerController(PlayerService playerService, AuthService authService) {
        this.playerService = playerService;
        this.authService = authService;
    }
    @GetMapping
    public List<PlayerResponseDto> allPlayers() {
        return playerService.getAllPlayers();
    }

    // 선수 검색 + 페이징
    @GetMapping
    public Page<PlayerDto> getPlayers(
            @RequestParam(required = false) String name,            // 검색어 없으면 전체 조회
            @RequestParam(defaultValue = "0") int page,             // 페이지 번호
            @RequestParam(defaultValue = "10") int size,            // 페이지 크기
            @RequestParam(defaultValue = "name") String sortBy,     // 정렬 기준 컬럼
            @RequestParam(defaultValue = "asc") String direction    // asc / desc
    ) {
        return playerService
                .searchPlayers(name, page, size, sortBy, direction)
                .map(PlayerDto::from);
    }

    @GetMapping("/{id}")
    public PlayerResponseDto getPlayer(@PathVariable Long id) {
        PlayerEntity e = playerService.getPlayEntity(id);
        return PlayerResponseDto.from(e);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addPlayer(
            Authentication authentication,
            @RequestPart("player") PlayerRequestDto dto,
            @RequestPart(value = "media", required = false) MultipartFile file
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }

        String username = authentication.getName(); // logout에서 했던 것과 동일

        // AuthService가 사용자 정보를 가져올 수 있다는 전제
        User user = authService.loadUserByUsername(username);

        if (user == null) {
            return new ResponseEntity<>("사용자 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        if (!user.isAdmin()) {
            return new ResponseEntity<>("관리자만 선수 등록이 가능합니다.", HttpStatus.FORBIDDEN);
        }

        Long id = playerService.createPlayer(dto, file);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

}
