package com.example.first.service;


import com.example.first.dto.request.UserRequestDto;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public void updateUser(UserRequestDto dto) {
        User user = userRepository.findByUsername(dto.getUsername()).orElseThrow();
        user.updateDisplayName(dto.getDisplay());
        userRepository.save(user);
    }
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
