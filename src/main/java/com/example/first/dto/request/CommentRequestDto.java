package com.example.first.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {

    private Long postId;      // NOT NULL
    private Long parentId;    // nullable (대댓글일 경우)
    private String content;
}
