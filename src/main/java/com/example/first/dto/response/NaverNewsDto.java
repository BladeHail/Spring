package com.example.first.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class NaverNewsDto {
    private List<Item> items;

    @Data
    public static class Item{
        private int id;
        private String title;
        private String link;
        private String description;
        private String pubDate;
    }
}
