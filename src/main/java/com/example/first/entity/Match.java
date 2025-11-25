package com.example.first.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String teamA;

    @Column(nullable = false, length = 100)
    private String teamB;

    @Column(nullable = false)
    private LocalDateTime matchDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MatchResult actualResult;

    @Column(nullable = false)
    private boolean predictionOpen = true;

    @Column(length = 200)
    private String description;
}
