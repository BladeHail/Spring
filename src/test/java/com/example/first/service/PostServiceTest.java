package com.example.first.service;

import com.example.first.dto.LiveBlock;
import com.example.first.dto.TextBlock;
import com.example.first.entity.Block;
import com.example.first.entity.Post;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class PostServiceTest {

    @Autowired
    PostService postService;

    @Test
    void post_create_with_blocks() {
        List<Block> blocks = List.of(
                new TextBlock("b1", "hello"),
                new LiveBlock("b2", "youtube", "abc123")
        );

        Post post = postService.create(1L, "title", blocks);

        assertThat(post.getId()).isNotNull();
        assertThat(post.getBlocks().size()).isEqualTo(2);
    }
}

