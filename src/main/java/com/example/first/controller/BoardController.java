package com.example.first.controller;

import com.example.first.dto.BoardDto;
import com.example.first.dto.BoardRequestDto;
import com.example.first.entity.BoardEntity;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final UserRepository userRepository;

    // 게시글 등록
    @PostMapping("/players/{playerId}/boards")
    public BoardDto create(
            @PathVariable Long playerId,
            @Valid @RequestBody BoardRequestDto request,
            Authentication authentication
    ){
     if (authentication == null || !authentication.isAuthenticated()) {

     }
     String currentUsername = authentication.getName();
     User user = userRepository.findByUsername(currentUsername)
             .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
     String displayAuthor = user.getEmail();
     if (displayAuthor == null || displayAuthor.isEmpty()) {
         displayAuthor = user.getUsername();
     }
     request.setAuthor(displayAuthor);
     request.setPlayerId(playerId);
     BoardEntity saved = boardService.create(request);
     return toDto(saved);
    }

    // 특정 선수 응원글 조회 추가
    @GetMapping("/players/{playerId}/boards")
    public List<BoardDto> listByPlayer(@PathVariable Long playerId) {
        return boardService.findByPlayerId(playerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // 게시글 상세 조회 (조회수 증가)
    @GetMapping("/{id}")
    public BoardDto detail(@PathVariable Long id) {
        return toDto(boardService.findById(id));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public BoardDto update(@PathVariable Long id, @RequestBody BoardRequestDto request) {
        BoardEntity updated = new BoardEntity(
                request.getTitle(),
                request.getContent(),
                request.getAuthor(),
                request.getMedia()
        );
        return toDto(boardService.update(id, updated));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "삭제완료";
    }

    // Entity -> Dto 변환
    private BoardDto toDto(BoardEntity board) {
        return BoardDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getAuthor())
                .views(board.getViews())
                .media(board.getMedia())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    // 최신순
    @GetMapping("/latest")
    public List<BoardDto> listLatest() {
        return boardService.listByLatest().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 조회수순
    @GetMapping("/popular")
    public List<BoardDto> listByViews() {
        return boardService.listByViews().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 페이징 목록: /boards/page?page=0&size=10&sortBy=createdAt&dir=desc
    public Page<BoardDto> pagedList(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10")int size,
                                    @RequestParam(defaultValue = "createdAt") String sortBy,
                                    @RequestParam(defaultValue = "desc") String dir) {
        Page<BoardEntity> entityPage = boardService.findPaged(page, size, sortBy, dir);
        List<BoardDto> dtoList = entityPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    // 검색 + 페이징: /boards/search?keyword=민지&page=0&size=10
    @GetMapping("/search")
    public Page<BoardDto> search(@RequestParam String keyword,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10")int size,
                                 @RequestParam(defaultValue = "createdAt") String sortBy,
                                 @RequestParam(defaultValue = "desc") String dir) {
        Page<BoardEntity> entityPage = boardService.search(keyword, page, size, sortBy, dir);
        List<BoardDto> dtoList = entityPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    // 이미지 포함 게시글 등록: multipart/form-data
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BoardDto createWithImage(@RequestParam String title,
                                    @RequestParam String content,
                                    @RequestParam String author,
                                    @RequestParam(required = false) MultipartFile file) throws IOException {
        BoardEntity saved = boardService.createWithImage(title, content, author, file);
        return toDto(saved);
    }
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BoardDto createWithImage(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile file,
            Authentication authentication // [추가]
    ) throws IOException {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("유저 없음"));


        String realAuthor = user.getEmail() != null ? user.getEmail() : user.getUsername();

        BoardEntity saved = boardService.createWithImage(title, content, realAuthor, file);
        return toDto(saved);
    }
}