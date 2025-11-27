package com.example.first.controller;

import com.example.first.repository.BoardRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
public class BoardController {
    private final BoardRepository repository;
    public BoardController(BoardRepository repository) {
        this.repository = repository;
    }


}
