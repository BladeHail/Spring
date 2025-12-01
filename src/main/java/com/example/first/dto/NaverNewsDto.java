package com.example.first.dto;

import lombok.Data;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;

import java.util.List;

@Data
public class NaverNewsDto {
    private List<Item> items;

    @Data
    public static class Item{
        private String id;
        private String title;
        private String link;
        private String description;
        private String pubDate;
    }
}
