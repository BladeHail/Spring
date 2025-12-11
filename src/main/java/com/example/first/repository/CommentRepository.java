package com.example.first.repository;

import com.example.first.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByBoardIdOrderByCreatedAtDesc(Long boardId);
}
