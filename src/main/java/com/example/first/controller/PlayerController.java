package com.example.first.controller;

import com.example.first.entity.PlayerEntity;
import com.example.first.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }
    @GetMapping
    public List<PlayerEntity> allPlayers() {
        return service.getAllPlayers();
    }
    @GetMapping("/{id}")
    public PlayerEntity playerEntity(@PathVariable Long id) {
        return  service.getPlayEntity(id);
    }
    @PostMapping
    public PlayerEntity addPlayer(@RequestBody PlayerEntity playerEntity) {
        return service.savePlayer(playerEntity);
    }
}
