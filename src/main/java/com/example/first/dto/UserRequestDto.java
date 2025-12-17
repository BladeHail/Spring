package com.example.first.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {
    private String username;
    @NotBlank(message = "닉네임은 비워둘 수 없습니다")
    private String display;
}
