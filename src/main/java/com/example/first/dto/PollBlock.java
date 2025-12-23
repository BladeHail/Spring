package com.example.first.dto;

import com.example.first.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollBlock implements Block {

    private String id;
    private final String type = "poll";
    private String question;
    private List<String> options;
}

