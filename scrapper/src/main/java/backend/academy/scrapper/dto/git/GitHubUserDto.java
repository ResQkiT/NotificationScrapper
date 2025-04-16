package backend.academy.scrapper.dto.git;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubUserDto(@JsonProperty("login") String username) {}
