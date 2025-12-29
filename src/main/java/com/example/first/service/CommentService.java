package com.example.first.service;

import com.example.first.dto.request.CommentRequestDto;
import com.example.first.dto.response.CommentResponseDto;
import com.example.first.entity.Comment;
import com.example.first.entity.Post;
import com.example.first.entity.User;
import com.example.first.repository.CommentRepository;
import com.example.first.repository.PostRepository;
import com.example.first.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentResponseDto create(Authentication auth, CommentRequestDto dto) {

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthorized");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new IllegalStateException("Post not found"));

        Comment parent = null;
        if (dto.getParentId() != null) {
            parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalStateException("Parent comment not found"));

            // 안전장치: parent도 같은 Post에 속해야 함
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new IllegalStateException("Invalid parent comment");
            }
        }

        Comment comment = new Comment(
                post,
                parent,
                user.getUsername(),
                dto.getContent()
        );

        Comment saved = commentRepository.save(comment);
        return toDto(saved);
    }

    @Transactional
    public CommentResponseDto update(Authentication auth, Long id, CommentRequestDto dto) {
        if(auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthorized");
        }
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new IllegalStateException("Comment not found"));
        comment.setContent(dto.getContent());
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment).toDto();
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> findByPost(Long postId, Pageable pageable) {
        return commentRepository
                .findByPostIdAndDeletedFalse(postId, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Comment findById(Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        return comment.orElseThrow(() -> new IllegalStateException("Comment not found"));
    }

    public void delete(Authentication auth, Long commentId) {

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthorized");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalStateException("Comment not found"));

        // notYourBusiness 로직을 그대로 적용할 지점
        boolean isOwner = user.getUsername().equals(comment.getAuthor());
        boolean isAdmin = user.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("Not your business");
        }
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    // ---- Entity → DTO ----
    private CommentResponseDto toDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .author(comment.getAuthor())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .deleted(comment.isDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
