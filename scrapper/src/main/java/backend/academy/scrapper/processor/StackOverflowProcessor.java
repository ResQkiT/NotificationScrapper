package backend.academy.scrapper.processor;

import backend.academy.scrapper.clients.api.StackOverflowApiClient;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowAnswerDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowAnswersListDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowCommentDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowCommentsListDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowResponseDto;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.StackOverflowLink;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.StackOverflowLinkService;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowProcessor extends Processor {

    private final StackOverflowApiClient stackOverflowClient;
    private final StackOverflowLinkService stackOverflowLinkService;

    public StackOverflowProcessor(
            StackOverflowApiClient stackOverflowClient,
            LinkService linkService,
            StackOverflowLinkService stackOverflowLinkService) {
        super("stackoverflow.com", linkService);
        this.stackOverflowClient = stackOverflowClient;
        this.stackOverflowLinkService = stackOverflowLinkService;
    }

    @Override
    public String process(Link link) {

        StackOverflowAnswersListDto answers = fetchAnswers(link);
        StackOverflowCommentsListDto comments = fetchComments(link);

        Optional<StackOverflowAnswerDto> lastAnswer = getLastAnswer(answers);
        Optional<StackOverflowCommentDto> lastComment = getLastComment(comments);

        if (lastAnswer.isEmpty() && lastComment.isEmpty()) {
            return null;
        }

        StackOverflowLink relevantLink = buildRelevantLink(link, lastAnswer, lastComment);
        return handleUpdates(link, relevantLink, lastAnswer, lastComment);
    }

    private String handleUpdates(
            Link link,
            StackOverflowLink newLink,
            Optional<StackOverflowAnswerDto> lastAnswer,
            Optional<StackOverflowCommentDto> lastComment) {

        StringBuilder response = new StringBuilder();
        boolean hasUpdates = false;
        StackOverflowLink existingLink =
                stackOverflowLinkService.findById(link.id()).orElse(null);

        if (isFirstTimeProcessing(link) || existingLink == null) {
            stackOverflowLinkService.createLink(newLink);
            service().onUpdateLink(link);
            service().onTouchLink(link);
            return null;
        }

        if (lastAnswer.isPresent()) {
            StackOverflowAnswerDto answer = lastAnswer.get();
            if (!Objects.equals(answer.answerId(), existingLink.answerLastId())) {
                response.append("Появился новый ответ\n");
                response.append("Автор: " + answer.owner().displayName() + "\n");
                response.append("Дата создания: " + answer.createdAt() + "\n");
                response.append("Описание: ").append(cutBody(answer.body())).append("\n");
                hasUpdates = true;
            }
        }

        if (lastComment.isPresent()) {
            StackOverflowCommentDto comment = lastComment.get();
            if (!Objects.equals(comment.commendId(), existingLink.commentId())) {
                response.append("Появился новый комментарий\n");
                response.append("Автор: " + comment.owner().displayName() + "\n");
                response.append("Дата создания: " + comment.createdAt() + "\n");
                response.append("Описание: ").append(cutBody(comment.body())).append("\n");
                hasUpdates = true;
            }
        }

        if (hasUpdates) {
            stackOverflowLinkService.updateLink(newLink);
            service().onTouchLink(link);
            service().onUpdateLink(link);
            return response.toString();
        }
        service().onTouchLink(link);

        return null;
    }

    private StackOverflowLink buildRelevantLink(
            Link link, Optional<StackOverflowAnswerDto> answer, Optional<StackOverflowCommentDto> comment) {
        StackOverflowLink stackOverflowLink = new StackOverflowLink();
        stackOverflowLink.id(link.id());

        answer.ifPresent(a -> {
            stackOverflowLink
                    .answerLastId(a.answerId())
                    .answerLastUsername(a.owner() != null ? a.owner().displayName() : "Unknown")
                    .answerCreatedAt(a.createdAt().atOffset(ZoneOffset.UTC))
                    .answerPreviewDescription(cutBody(a.body()));
        });

        comment.ifPresent(c -> {
            stackOverflowLink
                    .commentId(c.commendId())
                    .commentLastUsername(c.owner() != null ? c.owner().displayName() : "Unknown")
                    .commentCreatedAt(c.createdAt().atOffset(ZoneOffset.UTC))
                    .commentPreviewDescription(cutBody(c.body()));
        });

        return stackOverflowLink;
    }

    private Optional<StackOverflowAnswerDto> getLastAnswer(StackOverflowAnswersListDto dto) {
        return (dto != null && !dto.answers().isEmpty())
                ? Optional.of(dto.answers().getFirst())
                : Optional.empty();
    }

    private Optional<StackOverflowCommentDto> getLastComment(StackOverflowCommentsListDto dto) {
        return (dto != null && !dto.comments().isEmpty())
                ? Optional.of(dto.comments().getFirst())
                : Optional.empty();
    }

    private StackOverflowResponseDto fetchQuestionInfo(Link link) {
        URI uri = URI.create(link.url());
        Long questionId = extractQuestionId(uri.getPath());

        var response = stackOverflowClient.getQuestionInfo(questionId);
        assertSuccess(response, new RuntimeException("Cant fetch question info: StackOverflow API is unavailable"));
        return response.getBody();
    }

    private StackOverflowAnswersListDto fetchAnswers(Link link) {
        URI uri = URI.create(link.url());
        Long questionId = extractQuestionId(uri.getPath());

        var response = stackOverflowClient.getQuestionAnswers(questionId);
        assertSuccess(response, new RuntimeException("Cant fetch question answers : StackOverflow API is unavailable"));

        return response.getBody();
    }

    private StackOverflowCommentsListDto fetchComments(Link link) {
        URI uri = URI.create(link.url());
        Long questionId = extractQuestionId(uri.getPath());

        var response = stackOverflowClient.getQuestionComments(questionId);
        assertSuccess(
                response, new RuntimeException("Cant fetch question comments : StackOverflow API is unavailable"));
        return response.getBody();
    }

    public Long extractQuestionId(String url) {
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("questions".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return Long.parseLong(parts[i + 1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid question ID");
                }
            }
        }
        throw new IllegalArgumentException("Invalid Stack Overflow URL");
    }
}
