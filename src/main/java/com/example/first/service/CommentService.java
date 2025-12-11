package com.example.first.service;

import com.example.first.dto.CommentRequestDto;
import com.example.first.dto.CommentResponseDto;
import com.example.first.entity.BoardEntity;
import com.example.first.entity.CommentEntity;
import com.example.first.repository.BoardRepository;
import com.example.first.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    // 욕설 목록
    private final List<String> badWords = List.of(
            "씨발", "병신", "개새끼", "좆", "썅", "염병", "ㅅㅂ", "ㅈㄹ", "fuck"
    );

    // 욕설 필터링 메서드
    private String filter(String content) {
        String result = content;
        for (String word : badWords) {
            if (result.contains(word)) {
                result = result.replace(word, "***");
            }
        }
        return result;
    }

    // 댓글 생성
    public CommentResponseDto create(Long boardId, CommentRequestDto request) {

        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        String filtered = filter(request.getContent());

        CommentEntity comment = CommentEntity.builder()
                .content(filtered)
                .author(request.getAuthor())
                .createdAt(LocalDateTime.now())
                .board(board)
                .build();

        CommentEntity saved = commentRepository.save(comment);

        return CommentResponseDto.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .author(saved.getAuthor())
                .createdAt(saved.getCreatedAt())
                .boardId(boardId)
                .build();
    }

    // 댓글 조회
    public List<CommentResponseDto> getComments(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedAtDesc(boardId)
                .stream()
                .map(c -> CommentResponseDto.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .author(c.getAuthor())
                        .createdAt(c.getCreatedAt())
                        .boardId(boardId)
                        .build()
                )
                .toList();
    }
}
