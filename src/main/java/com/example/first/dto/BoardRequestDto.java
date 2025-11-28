package com.example.first.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardRequestDto {
    @NotBlank String title;
    @NotBlank String content;
    @NotBlank String author;
    String media;
}
