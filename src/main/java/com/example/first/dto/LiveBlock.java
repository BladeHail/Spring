package com.example.first.dto;

import com.example.first.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveBlock implements Block {

    private String id;
    private final String type = "live";
    private String provider;
    private String resourceId;
}

