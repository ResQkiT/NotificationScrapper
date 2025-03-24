package backend.academy.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record StackOverflowQuestionDto(
    String title,
    @JsonProperty("last_activity_date") OffsetDateTime lastActivityDate,
    @JsonProperty("answer_count") Long answerCount,
    Long score
){
}
