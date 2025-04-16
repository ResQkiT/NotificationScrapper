package backend.academy.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowUserDto(
        @JsonProperty("display_name") String displayName, @JsonProperty("user_id") Long userId) {}
