package com.example.first.blocks;

import com.example.first.dto.LiveBlock;
import com.example.first.dto.PollBlock;
import com.example.first.dto.TextBlock;
import com.example.first.entity.Block;
import com.example.first.utils.BlockListConverter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlockConverterTest {

    @Test
    void block_round_trip_test() {
        BlockListConverter converter = new BlockListConverter();

        List<Block> original = List.of(
                new TextBlock("b1", "hello"),
                new LiveBlock("b2", "youtube", "abc123"),
                new PollBlock("b3", "누가 이길까요?", List.of("A", "B"))
        );

        String json = converter.convertToDatabaseColumn(original);
        List<Block> restored = converter.convertToEntityAttribute(json);

        assertThat(restored).hasSize(3);
        assertThat(restored.get(0)).isInstanceOf(TextBlock.class);
        assertThat(restored.get(1)).isInstanceOf(LiveBlock.class);
        assertThat(restored.get(2)).isInstanceOf(PollBlock.class);
    }
}

