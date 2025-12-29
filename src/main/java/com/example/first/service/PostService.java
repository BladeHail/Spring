package com.example.first.service;
import com.example.first.dto.LiveBlock;
import com.example.first.dto.PollBlock;
import com.example.first.dto.VideoBlock;
import com.example.first.entity.Block;
import com.example.first.entity.Post;
import com.example.first.repository.PostRepository;
import com.example.first.repository.VideoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MatchService matchService;
    private final LiveYtService liveYtService;
    private final VideoRepository videoRepository;


    @Transactional
    public Post create(Long authorId, String title, List<Block> blocks) {

        validateBlocks(blocks);

        Post post = new Post(authorId, title, blocks);
        return postRepository.save(post);
    }

    @Transactional
    public void update(Long postId, String title, List<Block> blocks) {
        validateBlocks(blocks);
        Post post = postRepository.findById(postId).orElse(null);
        if(post != null) {
            Post newPost = new Post(post.getId(), post.getAuthorId(), title, blocks, post.getCreatedAt(), false);
            postRepository.save(newPost);
        }
    }

    @Transactional
    public void delete(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if(post != null) {
            Post newPost = new Post(post.getId(), post.getAuthorId(), post.getTitle(), post.getBlocks(), post.getCreatedAt(), true);
            postRepository.save(newPost);
        }
    }

    private void validateBlocks(List<Block> blocks) {
        for (Block block : blocks) {
            switch (block.getType()) {

                case "text" -> {
                    // 지금은 검증 없음
                }

                case "live" -> {
                    LiveBlock live = (LiveBlock) block;
                    try {
                        validateLiveBlock(live);
                    } catch(IllegalArgumentException ex) {
                        System.out.println(ex.getMessage());
                        throw ex;
                    } catch(RuntimeException e) {
                        System.out.println("Done");
                    }
                }

                case "prediction" -> {
                    PollBlock poll = (PollBlock) block;
                    if(!matchService.getMatchById(poll.getMatchId()).isPredictionOpen()){
                        throw new IllegalArgumentException("It's been so long...");
                    }
                }

                case "video" -> {
                    VideoBlock videoBlock = (VideoBlock) block;
                    if(!videoRepository.existsByVideoId(videoBlock.getVideoId())) {
                        throw new IllegalArgumentException("Video not allowed");
                    }
                }

                default -> throw new IllegalArgumentException(
                        "지원하지 않는 block type: " + block.getType()
                );
            }
        }
    }

    private void validateLiveBlock(LiveBlock block) {

        if (block.getVideoId() == null || block.getVideoId().isBlank()) {
            throw new IllegalArgumentException("videoId가 비어 있습니다");
        }
        String videoId = block.getVideoId();
        liveYtService.getLiveVideos().forEach(liveYt -> {
            if(liveYt.getVideoId().equals(videoId)) {
                throw new RuntimeException("됐는데 일단 처리");
            }
        });
        throw new IllegalArgumentException("허가되지 않은 videoId");
    }
}

