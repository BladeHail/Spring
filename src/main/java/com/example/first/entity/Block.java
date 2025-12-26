package com.example.first.entity;
import com.example.first.dto.LiveBlock;
import com.example.first.dto.PollBlock;
import com.example.first.dto.TextBlock;
import com.example.first.dto.VideoBlock;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextBlock.class, name = "text"),
        @JsonSubTypes.Type(value = LiveBlock.class, name = "live"),
        @JsonSubTypes.Type(value = PollBlock.class, name = "prediction"),
        @JsonSubTypes.Type(value = VideoBlock.class, name="video")
})
public interface Block {

    String getId();

    String getType();
}

