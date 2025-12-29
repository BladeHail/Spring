package com.example.first.service;

import com.example.first.dto.response.FeedItemDto;
import com.example.first.dto.response.LiveYtDto;
import com.example.first.entity.*;
import com.example.first.repository.*;
import com.example.first.utils.FeedItemType;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FeedService {

    private VideoRepository videoRepository;
    private PostRepository postRepository;
    private UserRepository userRepository;
    private MatchRepository matchRepository;
    private PredictionRepository predictionRepository;
    private LiveYtService liveYtService;
    @Getter
    private List<FeedItemDto> feeds;

    @PostConstruct
    public void init() {
        updateFeed(10);
    }

    @Scheduled(fixedRate = 1000 * 60 * 2)
    public void update() {
        updateFeed(10);
    }

    public void updateFeed(int size) {
        List<FeedItem> temp = new ArrayList<>();
        List<YoutubeVideo> Videos = videoRepository.findAll();
        for (YoutubeVideo video : Videos) {
            LocalDateTime publishedAt = video.getPublishedAt()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            FeedItem feeder = new FeedItem(
                    calculatePriority(publishedAt) * 10 + 3_000_000L,
                    FeedItemType.VIDEO,
                    publishedAt,
                    video.getTitle(),
                    video.getVideoId()
            );
            temp.add(feeder);
        }
        List<Post> Posts = postRepository.findAllByDeletedFalse();
        for (Post post : Posts) {
            User user = userRepository.findById(post.getAuthorId()).orElse(null);
            FeedItem feeder = new FeedItem(
                    calculatePriority(post.getCreatedAt()) * 10 + 1_000_000L,
                    FeedItemType.POST,
                    post.getCreatedAt(),
                    post.getTitle(),
                    (user != null) ? user.getUsername() + "{" + post.getId() : "게시글"
            );
            temp.add(feeder);
        }
        List<Match> Matches = matchRepository.findAll();
        for (Match match : Matches) {
            String title = "";
            title += match.getTeamA();
            title += " vs. ";
            title += match.getTeamB();
            String versus = "";
            versus += predictionRepository.countVotes(match.getId(), MatchResult.HOME_WIN);
            versus += " : ";
            versus += predictionRepository.countVotes(match.getId(), MatchResult.AWAY_WIN);
            FeedItem feeder = new FeedItem(
                    calculateMatchPriority(match.getMatchDate()),
                    FeedItemType.MATCH,
                    match.getMatchDate(),
                    title,
                    versus
            );
            temp.add(feeder);
        }
        List<LiveYtDto> Lives = liveYtService.getLiveVideos();
        for (LiveYtDto live : Lives) {
            FeedItem feeder = new FeedItem(
                    1_000_000_000_000_000L,
                    FeedItemType.LIVE,
                    LocalDateTime.now(),
                    live.getTitle(),
                    live.getVideoId()
            );
            temp.add(feeder);
        }
        List<FeedItemDto> result = new ArrayList<>();

        temp.stream()
                .sorted((a, b) -> Long.compare(b.getPriority(), a.getPriority()))
                .limit(size)
                .forEachOrdered(item -> result.add(
                        new FeedItemDto(
                                result.size(),
                                item.getItemType(),
                                item.getPublished(),
                                item.getTitle(),
                                item.getMetadata()
                        )
                ));
        feeds = result;
    }

    protected long calculatePriority(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0L;
        }
        return dateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
    protected long calculateMatchPriority(LocalDateTime matchDate) {
        LocalDateTime now = LocalDateTime.now();
        if (matchDate.isBefore(now)) {
            return 0L;
        }
        long base =
                calculatePriority(matchDate) * 50 + 4_000_000L;
        long minutes =
                java.time.Duration.between(now, matchDate).toMinutes();
        // 반감기: 1일
        double halfLifeMinutes = 1440.0;
        double decay = Math.exp(-minutes / halfLifeMinutes);
        return (long) (base * decay);
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedItem {
        private long priority;
        private FeedItemType itemType;
        private LocalDateTime published;
        private String title;
        private String metadata;
    }
}

