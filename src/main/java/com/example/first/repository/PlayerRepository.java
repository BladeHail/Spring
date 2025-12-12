package com.example.first.repository;

import com.example.first.entity.PlayerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    Page<PlayerEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
