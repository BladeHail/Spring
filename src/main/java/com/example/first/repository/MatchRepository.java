package com.example.first.repository;

import com.example.first.entity.Match;
import com.example.first.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByPredictionOpenTrueOrderByMatchDateAsc();
    List<Match> findAllByOrderByMatchDateDesc();

}
