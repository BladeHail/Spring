package com.example.first.repository;

import com.example.first.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("Repository 테스트")
class RepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    private User user;
    private Match match;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .provider(AuthProvider.LOCAL)
                .build();
        entityManager.persist(user);

        match = Match.builder()
                .teamA("한국")
                .teamB("일본")
                .matchDate(LocalDateTime.now().plusDays(1))
                .predictionOpen(true)
                .description("테스트 경기")
                .build();
        entityManager.persist(match);

        entityManager.flush();
    }

    @Test
    @DisplayName("UserRepository - username으로 사용자 찾기")
    void findByUsername() {
        // when
        Optional<User> found = userRepository.findByUsername("testuser");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("UserRepository - username 존재 여부 확인")
    void existsByUsername() {
        // when
        boolean exists = userRepository.existsByUsername("testuser");
        boolean notExists = userRepository.existsByUsername("nonexistent");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("UserRepository - provider와 providerId로 사용자 찾기")
    void findByProviderAndProviderId() {
        // given
        User oauthUser = User.builder()
                .username("kakaouser")
                .provider(AuthProvider.KAKAO)
                .providerId("12345")
                .build();
        entityManager.persist(oauthUser);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByProviderAndProviderId(
                AuthProvider.KAKAO, "12345");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("kakaouser");
        assertThat(found.get().getProvider()).isEqualTo(AuthProvider.KAKAO);
    }

    @Test
    @DisplayName("MatchRepository - 예측 가능한 경기 조회")
    void findByPredictionOpenTrue() {
        // given
        Match closedMatch = Match.builder()
                .teamA("미국")
                .teamB("중국")
                .matchDate(LocalDateTime.now().plusDays(2))
                .predictionOpen(false)
                .build();
        entityManager.persist(closedMatch);
        entityManager.flush();

        // when
        List<Match> openMatches = matchRepository.findByPredictionOpenTrueOrderByMatchDateAsc();

        // then
        assertThat(openMatches).hasSize(1);
        assertThat(openMatches.get(0).isPredictionOpen()).isTrue();
        assertThat(openMatches.get(0).getTeamA()).isEqualTo("한국");
    }

    @Test
    @DisplayName("MatchRepository - 전체 경기 날짜 역순 조회")
    void findAllByOrderByMatchDateDesc() {
        // given
        Match futureMatch = Match.builder()
                .teamA("미국")
                .teamB("중국")
                .matchDate(LocalDateTime.now().plusDays(5))
                .predictionOpen(true)
                .build();
        entityManager.persist(futureMatch);
        entityManager.flush();

        // when
        List<Match> matches = matchRepository.findAllByOrderByMatchDateDesc();

        // then
        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).getMatchDate()).isAfter(matches.get(1).getMatchDate());
    }

    @Test
    @DisplayName("PredictionRepository - 사용자와 경기로 예측 존재 여부 확인")
    void existsByUserIdAndMatch() {
        // given
        Prediction prediction = Prediction.builder()
                .user(user)
                .match(match)
                .predictedResult(MatchResult.HOME_WIN)
                .predictedAt(LocalDateTime.now())
                .build();
        entityManager.persist(prediction);
        entityManager.flush();

        // when
        boolean exists = predictionRepository.existsPredictionByUserIdAndMatch(user.getId(), match);

        Match anotherMatch = Match.builder()
                .teamA("미국")
                .teamB("중국")
                .matchDate(LocalDateTime.now().plusDays(2))
                .predictionOpen(true)
                .build();
        entityManager.persist(anotherMatch);
        entityManager.flush();

        boolean notExists = predictionRepository.existsPredictionByUserIdAndMatch(user.getId(), anotherMatch);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("PredictionRepository - 사용자 예측 목록 조회 (최신순)")
    void findByUserIdOrderByPredictedAtDesc() {
        // given
        Prediction prediction1 = Prediction.builder()
                .user(user)
                .match(match)
                .predictedResult(MatchResult.HOME_WIN)
                .predictedAt(LocalDateTime.now().minusHours(2))
                .build();

        Match match2 = Match.builder()
                .teamA("미국")
                .teamB("중국")
                .matchDate(LocalDateTime.now().plusDays(2))
                .predictionOpen(true)
                .build();
        entityManager.persist(match2);

        Prediction prediction2 = Prediction.builder()
                .user(user)
                .match(match2)
                .predictedResult(MatchResult.AWAY_WIN)
                .predictedAt(LocalDateTime.now().minusHours(1))
                .build();

        entityManager.persist(prediction1);
        entityManager.persist(prediction2);
        entityManager.flush();

        // when
        List<Prediction> predictions = predictionRepository
                .findByUserIdOrderByPredictedAtDesc(user.getId());

        // then
        assertThat(predictions).hasSize(2);
        assertThat(predictions.get(0).getPredictedAt())
                .isAfter(predictions.get(1).getPredictedAt());
    }

    @Test
    @DisplayName("PredictionRepository - 사용자별 예측 수 카운트")
    void countByUserId() {
        // given
        Prediction prediction1 = Prediction.builder()
                .user(user)
                .match(match)
                .predictedResult(MatchResult.HOME_WIN)
                .predictedAt(LocalDateTime.now())
                .build();
        entityManager.persist(prediction1);
        entityManager.flush();

        // when
        long count = predictionRepository.countByUserId(user.getId());

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("PredictionRepository - 완료된 경기 예측 수 카운트")
    void countByUserIdAndMatch_ActualResultNotNull() {
        // given
        match.setActualResult(MatchResult.HOME_WIN);
        entityManager.persist(match);

        Prediction prediction = Prediction.builder()
                .user(user)
                .match(match)
                .predictedResult(MatchResult.HOME_WIN)
                .predictedAt(LocalDateTime.now())
                .build();
        entityManager.persist(prediction);
        entityManager.flush();

        // when
        long count = predictionRepository.countByUserIdAndMatch_ActualResultNotNull(user.getId());

        // then
        assertThat(count).isEqualTo(1L);
    }
}