package com.example.first.dto.response;

import com.example.first.utils.FeedItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedItemDto {
    private int order;
    private FeedItemType itemType;
    private LocalDateTime published;
    private String title;
    private String metadata;
}
