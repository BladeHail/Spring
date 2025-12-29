package com.example.first.controller;

import com.example.first.dto.request.CommentRequestDto;
import com.example.first.dto.response.CommentResponseDto;
import com.example.first.entity.Comment;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import com.example.first.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserRepository userRepository;

    // 댓글 / 대댓글 생성
    @PostMapping("/{postId}")
    public ResponseEntity<?> create(
            Authentication auth,
            @PathVariable Long postId,
            @RequestBody CommentRequestDto dto
    ) {
        if(postId == null || !Objects.equals(postId, dto.getPostId())) {
            return new ResponseEntity<>("잘못된 요청", HttpStatus.BAD_REQUEST);
        }
        CommentResponseDto created = commentService.create(auth, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/{postId}/{parentId}")
    public ResponseEntity<?> createReply(
            Authentication auth,
            @PathVariable Long postId,
            @PathVariable Long parentId,
            @RequestBody CommentRequestDto dto
    ) {
        if(postId == null || !Objects.equals(postId, dto.getPostId())) {
            return new ResponseEntity<>("잘못된 요청", HttpStatus.BAD_REQUEST);
        }
        if(parentId == null || !Objects.equals(parentId, dto.getParentId())) {
            return new ResponseEntity<>("올바르지 않은 답글 형식", HttpStatus.BAD_REQUEST);
        }
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

    @GetMapping("/edit/{commentId}")
    public ResponseEntity<?> getComment(
            Authentication auth,
            @PathVariable Long commentId
    ){
        if(notYourBusiness(auth, commentId, false)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Comment comment = commentService.findById(commentId);
        CommentResponseDto dto = comment.toDto();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> edit(
            Authentication auth,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto dto
    ) {
        if(notYourBusiness(auth, commentId, false)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        CommentResponseDto created = commentService.update(auth, commentId, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // 댓글 삭제 (soft delete)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> delete(
            Authentication auth,
            @PathVariable Long commentId
    ) {
        if(notYourBusiness(auth, commentId, true)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        commentService.delete(auth, commentId);
        return ResponseEntity.noContent().build();
    }

    private boolean notYourBusiness(Authentication auth, Long id, boolean allowAdmin) {
        if(auth == null || !auth.isAuthenticated()) {
            System.out.println("Not authenticated");
            return true;
        }
        Optional<User> user = userRepository.findByUsername(auth.getName());
        Comment comment = commentService.findById(id);
        if(user.isEmpty() || comment == null) {
            System.out.println("No such user or comment");
            return true;
        }
        if(!user.get().getUsername().equals(comment.getAuthor())) {
            if(!user.get().isAdmin()) {
                System.out.println("Not your business");
                return true;
            }
            else if(!allowAdmin) {
                System.out.println("Even if you are an admin, it's not your business");
                return true;
            }
            return false;
        }
        return false;
    }
}
