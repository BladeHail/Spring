package com.project.first.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class PlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 이름

    @Column(nullable = false)
    private String body; // 신체정보 예: "175cm 64kg"

    @Column(nullable = false)
    private String type; // 종목 예: "500m, 1000m, 1500m"

    @Column(nullable = false)
    private String team;

    @Column
    private String media;

    @ElementCollection
    @Column
    private List<String> awards;

    public PlayerEntity() {}

    public PlayerEntity(String name, String body, String type, String team, String media,
                        List<String> awards) {
        this.name = name;
        this.body = body;
        this.type = type;
        this.team = team;
        this.media = media;
        this.awards = awards;
    }
}
