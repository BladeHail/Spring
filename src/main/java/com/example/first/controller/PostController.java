package com.example.first.controller;

import com.example.first.dto.request.CreatePostRequestDto;
import com.example.first.dto.response.AdminPostListDto;
import com.example.first.dto.response.PostListDto;
import com.example.first.dto.response.PostResponseDto;
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

import java.util.Objects;
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
    @GetMapping("/admin")
    public Page<AdminPostListDto> forceList(Pageable pageable, Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            System.out.println("Not authenticated");
            return null;
        }
        Optional<User> user = userService.findUserByUsername(auth.getName());
        if(user.isEmpty() || !user.get().isAdmin()) {
            System.out.println("Not admin");
            return null;
        }
        return postRepository.forceFindPostList(pageable);
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
        return new ResponseEntity<>(post.toDto(), HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Optional<Post> post = postRepository.findById(id);
        if(post.isPresent() && !post.get().isDeleted()) {
            PostResponseDto dto = post.get().toDto();
            User author = userService.getUserById(post.get().getAuthorId());
            dto.setAuthorName(author.getUsername());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<>("post not found", HttpStatus.NOT_FOUND);
    }
    @GetMapping("/{id}/admin")
    public ResponseEntity<?> get(Authentication auth, @PathVariable Long id) {
        if(auth == null || !auth.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> user = userService.findUserByUsername(auth.getName());
        if(user.isEmpty() ||  !user.get().isAdmin()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Post> post = postRepository.findById(id);
        if(post.isPresent()) {
            PostResponseDto dto = post.get().toDto();
            User author = userService.getUserById(post.get().getAuthorId());
            dto.setAuthorName(author.getUsername());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<>("post not found", HttpStatus.NOT_FOUND);
    }
    @GetMapping("/{id}/edit")
    public ResponseEntity<?> getEdit(Authentication auth, @PathVariable Long id) {
        if(notYourBusiness(auth, id, false)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Post post = postRepository.findById(id).get(); //Cannot be null since notYourBusiness tests first
        PostResponseDto dto = post.toDto();
        User user = userService.getUserById(post.getAuthorId());
        dto.setAuthorName(user.getUsername());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> edit(Authentication auth, @PathVariable Long id, @RequestBody CreatePostRequestDto dto) {
        if(notYourBusiness(auth, id, false)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        postService.update(id, dto.getTitle(), dto.getBlocks());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        if(notYourBusiness(auth, id, true)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        postService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private boolean notYourBusiness(Authentication auth, Long id, boolean allowAdmin) {
        if(auth == null || !auth.isAuthenticated()) {
            System.out.println("Not authenticated");
            return true;
        }
        Optional<User> user = userService.findUserByUsername(auth.getName());
        Optional<Post> post = postRepository.findById(id);
        if(user.isEmpty() || post.isEmpty()) {
            System.out.println("No such user or post");
            return true;
        }
        if(!Objects.equals(user.get().getId(), post.get().getAuthorId())) {
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
        //return !(Objects.equals(user.get().getId(), post.get().getAuthorId()) || (user.get().isAdmin() && allowAdmin));
    }
}
