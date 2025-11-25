package com.project.first.service;

import com.project.first.entity.PlayerEntity;
import com.project.first.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {
    private final PlayerRepository repository;

    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }
    public List<PlayerEntity> getAllPlayers(){
        return repository.findAll();
    }
    public PlayerEntity getPlayEntity(Long id) {
        return repository.findById(id).orElse(null);
    }
    public PlayerEntity savePlayer(PlayerEntity playerEntity) {
        return repository.save(playerEntity);
    }
}
