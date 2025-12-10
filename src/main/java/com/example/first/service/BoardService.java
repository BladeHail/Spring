package com.example.first.service;

import com.example.first.dto.BoardRequestDto;
import com.example.first.entity.BoardEntity;
import com.example.first.entity.PlayerEntity;
import com.example.first.exception.BoardNotFoundException;
import com.example.first.repository.BoardRepository;
import com.example.first.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final PlayerRepository playerRepository;

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    // 생성
    public BoardEntity create(BoardRequestDto request) {

        PlayerEntity player = playerRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new RuntimeException("선수 없음"));

        BoardEntity board = BoardEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(request.getAuthor())
                .media(request.getMedia())
                .views(0)
                .createdAt(now())
                .updatedAt(now())
                .player(player)
                .build();

        return boardRepository.save(board);
    }

    // 상세 + 조회수 증가
    public BoardEntity findById(Long id) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);
        boardEntity.setViews(boardEntity.getViews() + 1);
        return boardRepository.save(boardEntity);
    }

    // 수정
    public void update(Long id, BoardEntity updated) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);
        boardEntity.setTitle(updated.getTitle());
        boardEntity.setContent(updated.getContent());
        boardEntity.setUpdatedAt(now());
        boardRepository.save(boardEntity);
    }

    // 삭제 대신 숨김 플래그만 변경
    public void delete(Long id) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);
        boardEntity.setUpdatedAt(now());
        boardEntity.setDeleted(true);
        boardRepository.save(boardEntity);
    }

    public List<BoardEntity> listByLatest() {
        return boardRepository.findAllByOrderByCreatedAtDesc();
    }
    public List<BoardEntity> listByViews() {
        return boardRepository.findAllByOrderByViewsDesc();
    }
    public Page<BoardEntity> findByPlayerIdPaged(Long playerId, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return boardRepository.findByPlayerIdAndDeletedFalse(playerId, pageable);
    }
    public Page<BoardEntity> findPaged(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return boardRepository.findAll(pageable);
    }
    public Page<BoardEntity> search(String keyword, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return boardRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                        keyword, keyword, keyword, pageable
                );
    }
}
