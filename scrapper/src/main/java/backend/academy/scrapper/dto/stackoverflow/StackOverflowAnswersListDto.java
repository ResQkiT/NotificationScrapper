package backend.academy.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackOverflowAnswersListDto (
    @JsonProperty("items") List<StackOverflowAnswerDto> answers
){
}
