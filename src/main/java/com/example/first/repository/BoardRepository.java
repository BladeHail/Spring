package com.example.first.repository;

import com.example.first.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity,Long> {
    List<BoardEntity> findAllByOrderByCreatedAtDesc(); // 최신순 조회
    List<BoardEntity> findAllByOrderByViewsDesc(); // 조회수순 조회
    List<BoardEntity> findByPlayerIdOrderByCreatedAtDesc(Long playerId); // 선수별 응원글
    List<BoardEntity> findByDeletedFalse();
    List<BoardEntity> findByAuthorAndDeletedFalse(String author);
    Page<BoardEntity> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String content, String author, Pageable pageable
    );
    Page<BoardEntity> findByPlayerId(Long playerId, Pageable pageable);
    Page<BoardEntity> findByPlayerIdAndDeletedFalse(Long playerId, Pageable pageable);
}
