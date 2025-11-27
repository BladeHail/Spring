package com.example.first.service;

import com.example.first.entity.BoardEntity;
import com.example.first.exception.BoardNotFoundException;
import com.example.first.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    // 생성
    public BoardEntity create(BoardEntity boardEntity) {
        boardEntity.setCreatedAt(LocalDateTime.now());
        boardEntity.setUpdatedAt(LocalDateTime.now());
        return boardRepository.save(boardEntity);
    }

    // 목록
    public List<BoardEntity> findAll() {
        return boardRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // 상세 + 조회수 증가
    public BoardEntity findById(Long id) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);
        boardEntity.setViews(boardEntity.getViews() + 1);
        return boardRepository.save(boardEntity);
    }

    // 수정
    public BoardEntity update(Long id, BoardEntity updated) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);
        boardEntity.setTitle(updated.getTitle());
        boardEntity.setContent(updated.getContent());
        boardEntity.setUpdatedAt(LocalDateTime.now());
        return boardRepository.save(boardEntity);
    }

    // 삭제 대신 숨김 플래그만 변경
    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    public List<BoardEntity> listByLatest() {
        return boardRepository.findAllByOrderByCreatedAtDesc();
    }
    public List<BoardEntity> listByViews() {
        return boardRepository.findAllByOrderByViewsDesc();
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
    public BoardEntity createWithImage(String title, String content, String author,
                                       MultipartFile file) throws IOException {
        String mediaPath = null;
        if (file != null && !file.isEmpty()) {
            String folder = "src/main/resources/static/images/";
            Files.createDirectories(Paths.get(folder));
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(folder + filename);
            Files.write(path, file.getBytes());
            mediaPath = folder + filename;
        }
        BoardEntity board = BoardEntity.builder()
                .title(title)
                .content(content)
                .author(author)
                .media(mediaPath)
                .views(0)
                .createdAt(now())
                .updatedAt(now())
                .build();
        return boardRepository.save(board);
    }
}
