package backend.academy.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackOverflowCommentsListDto(@JsonProperty("items") List<StackOverflowCommentDto> comments) {}
