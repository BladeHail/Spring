package com.example.first.repository;

import com.example.first.entity.Match;
import com.example.first.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    boolean existsByUserIdAndMatch(Long userId, Match match);
    Optional<Prediction> findByUserIdAndMatch(Long userId, Match match);
    List<Prediction> findByUserIdOrderByPredictedAtDesc(Long userId);

    long countByUserId(Long userId);
    long countByUserIdAndMatch_ActualResultNotNull(Long userId);
}
