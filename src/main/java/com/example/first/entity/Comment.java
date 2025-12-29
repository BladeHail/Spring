package com.example.first.entity;

import com.example.first.dto.response.CommentResponseDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 모든 댓글은 반드시 Post에 속함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 대댓글일 경우만 값이 존재
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted = false;

    // --- 편의 생성자 (선택) ---
    public Comment(Post post, Comment parent, String author, String content) {
        this.post = post;
        this.parent = parent;
        this.author = author;
        this.content = content;
    }

    public CommentResponseDto toDto() {
        if(this.parent == null) {
            return CommentResponseDto.builder()
                    .id(id)
                    .author(author)
                    .content(content)
                    .postId(post.getId())
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }
        else {
            return CommentResponseDto.builder()
                    .id(id)
                    .author(author)
                    .content(content)
                    .postId(post.getId())
                    .parentId(parent.getId())
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }
    }
}
