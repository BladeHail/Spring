package com.example.first.repository;

import com.example.first.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByIdAndDeletedFalse(Long id);
    List<PlayerEntity> findAllByDeletedFalse();
}
