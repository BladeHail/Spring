package com.example.first.controller;

import com.example.first.dto.request.CreatePostRequestDto;
import com.example.first.dto.response.PostListDto;
import com.example.first.entity.Post;
import com.example.first.entity.User;
import com.example.first.repository.PostRepository;
import com.example.first.service.PostService;
import com.example.first.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final UserService userService;
    @GetMapping
    public Page<PostListDto> list(Pageable pageable) {
        return postRepository.findPostList(pageable);
    }
    @PostMapping
    public ResponseEntity<?> create(Authentication auth, @RequestBody CreatePostRequestDto dto) {
        if(auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> author = userService.findUserByUsername(auth.getName());
        if(author.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Post post = postService.create(
                author.get().getId(),
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
