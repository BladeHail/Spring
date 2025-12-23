package com.example.first.utils;

import com.example.first.entity.Block;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class BlockListConverter
        implements AttributeConverter<List<Block>, String> {

    private static final ObjectMapper mapper =
            BlockObjectMapper.create();

    @Override
    public String convertToDatabaseColumn(List<Block> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Block JSON 직렬화 실패", e);
        }
    }

    @Override
    public List<Block> convertToEntityAttribute(String dbData) {
        try {
            JavaType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, Block.class);
            return mapper.readValue(dbData, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Block JSON 역직렬화 실패", e);
        }
    }
}

