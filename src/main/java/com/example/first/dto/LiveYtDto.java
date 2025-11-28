package com.example.first.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiveYtDto {
    private String title;
    private String videoId;
    private String channelId;
    private String channelName;
}
