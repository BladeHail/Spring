package com.example.first.controller;

import com.example.first.dto.request.CreatePostRequestDto;
import com.example.first.entity.Post;
import com.example.first.repository.PostRepository;
import com.example.first.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreatePostRequestDto dto) {
        Post post = postService.create(
                1L, // 임시 authorId
                dto.getTitle(),
                dto.getBlocks()
        );
        if(post == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(post, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Optional<Post> post = postRepository.findById(id);
        if(post.isPresent()) {
            return new ResponseEntity<>(post, HttpStatus.OK);
        }
        return new ResponseEntity<>("post not found", HttpStatus.NOT_FOUND);
    }
}
