package backend.academy.scrapper.dto.stackoverflow;

import java.util.List;

public record StackOverflowResponseDto(List<StackOverflowQuestionDto> items) {}
