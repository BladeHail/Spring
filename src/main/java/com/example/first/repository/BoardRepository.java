package com.example.first.repository;

import com.example.first.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity,Long> {
    List<BoardEntity> findAllByOrderByCreatedAtDesc();
    List<BoardEntity> findAllByOrderByViewsDesc();
    Page<BoardEntity> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String content, String author, Pageable pageable
    );
}
