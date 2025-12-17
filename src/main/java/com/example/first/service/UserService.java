package com.example.first.service;


import com.example.first.dto.UserRequestDto;
import com.example.first.entity.User;
import com.example.first.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    UserRepository userRepository;
    public void updateUser(UserRequestDto dto) {
        User user = userRepository.findByUsername(dto.getUsername()).orElseThrow();
        user.updateDisplayName(dto.getDisplay());
        userRepository.save(user);
    }
}
