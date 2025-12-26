package com.example.first.controller;

import com.example.first.dto.request.CommentRequestDto;
import com.example.first.dto.response.CommentResponseDto;
import com.example.first.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 / 대댓글 생성
    @PostMapping
    public ResponseEntity<CommentResponseDto> create(
            Authentication auth,
            @RequestBody CommentRequestDto dto
    ) {
        CommentResponseDto created = commentService.create(auth, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // 특정 Post 하위 댓글 조회 (Pageable)
    @GetMapping("/{postId}")
    public ResponseEntity<Page<CommentResponseDto>> listByPost(
            @PathVariable Long postId,
            Pageable pageable
    ) {
        Page<CommentResponseDto> page =
                commentService.findByPost(postId, pageable);
        return ResponseEntity.ok(page);
    }

    // 댓글 삭제 (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication auth,
            @PathVariable Long id
    ) {
        commentService.delete(auth, id);
        return ResponseEntity.noContent().build();
    }
}
