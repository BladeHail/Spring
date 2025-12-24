package com.example.first.service;
import com.example.first.dto.LiveBlock;
import com.example.first.dto.PollBlock;
import com.example.first.entity.Block;
import com.example.first.entity.Post;
import com.example.first.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MatchService matchService;


    @Transactional
    public Post create(Long authorId, String title, List<Block> blocks) {

        validateBlocks(blocks);

        Post post = new Post(authorId, title, blocks);
        return postRepository.save(post);
    }

    private void validateBlocks(List<Block> blocks) {
        for (Block block : blocks) {
            switch (block.getType()) {

                case "text" -> {
                    // 지금은 검증 없음
                }

                case "live" -> {
                    LiveBlock live = (LiveBlock) block;
                    validateLiveBlock(live);
                }

                case "prediction" -> {
                    PollBlock poll = (PollBlock) block;
                    if(!matchService.getMatchById(poll.getMatchId()).isPredictionOpen()){
                        throw new IllegalArgumentException("It's been so long...");
                    }
                }

                default -> throw new IllegalArgumentException(
                        "지원하지 않는 block type: " + block.getType()
                );
            }
        }
    }

    private void validateLiveBlock(LiveBlock block) {
        if (!"youtube".equals(block.getProvider())) {
            throw new IllegalArgumentException("지원하지 않는 provider");
        }

        if (block.getResourceId() == null || block.getResourceId().isBlank()) {
            throw new IllegalArgumentException("videoId가 비어 있습니다");
        }

        // TODO: 필요 시 여기서 “서버 허용 videoId” 검사
    }
}

