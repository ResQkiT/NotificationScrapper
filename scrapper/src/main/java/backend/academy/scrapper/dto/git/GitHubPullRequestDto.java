package backend.academy.scrapper.dto.git;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GitHubPullRequestDto(
        String title,
        @JsonProperty("id") Long id,
        @JsonProperty("user") GitHubUserDto user,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("body") String body) {

    public String preview() {
        return body != null ? body.substring(0, Math.min(200, body.length())) : "";
    }
}
