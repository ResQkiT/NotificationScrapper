package backend.academy.scrapper.dto.git;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GitHubResponseDto(
        @JsonProperty("full_name") String fullName,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("default_branch") String defaultBranch) {}
