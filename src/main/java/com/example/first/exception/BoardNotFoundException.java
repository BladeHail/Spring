package com.example.first.exception;

public class BoardNotFoundException extends RuntimeException {
    public BoardNotFoundException() {
        super("게시글을 찾을수 없습니다.");
    }
}
