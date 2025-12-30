package com.example.first.repository;

import com.example.first.entity.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VideoRepository
        extends JpaRepository<YoutubeVideo, Long> {

    boolean existsByVideoId(String videoId);
    YoutubeVideo findByVideoId(String videoId);
    List<YoutubeVideo> findAllByOrderByIdDesc();
}

