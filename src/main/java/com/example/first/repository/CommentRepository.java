package com.example.first.repository;

import com.example.first.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 Post 하위의 댓글(1차 + 대댓글 포함)을 페이징 조회
    Page<Comment> findByPostIdAndDeletedFalse(
            Long postId,
            Pageable pageable
    );
}

