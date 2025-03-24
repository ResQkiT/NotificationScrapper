package backend.academy.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record StackOverflowCommentDto(
    @JsonProperty("comment_id") Long commendId,
    @JsonProperty("owner") StackOverflowUserDto owner,
    @JsonProperty("creation_date") Instant createdAt,
    @JsonProperty("body") String body
) {
}
