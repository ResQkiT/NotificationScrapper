package backend.academy.scrapper.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record StackOverflowQuestionResponseDto(List<Question> items) {
    public record Question(
        String title,
        OffsetDateTime lastActivityDate,
        Long answerCount,
        Long score
    ) {
    }
}
