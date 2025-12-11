package com.example.first.controller;

import com.example.first.dto.CommentRequestDto;
import com.example.first.dto.CommentResponseDto;
import com.example.first.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{boardId}")
    public CommentResponseDto create(
            @PathVariable Long boardId,
            @RequestBody CommentRequestDto request
    ) {
        return commentService.create(boardId, request);
    }

    // 댓글 조회
    @GetMapping("/{boardId}")
    public List<CommentResponseDto> getComments(@PathVariable Long boardId) {
        return commentService.getComments(boardId);
    }
}
