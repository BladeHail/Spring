package com.example.first.dto.request;

import com.example.first.entity.Block;
import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequestDto {
    private String title;
    private List<Block> blocks;
}
