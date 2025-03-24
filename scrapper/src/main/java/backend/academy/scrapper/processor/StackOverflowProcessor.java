package backend.academy.scrapper.processor;

import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowAnswerDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowAnswersListDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowCommentDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowCommentsListDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowResponseDto;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.StackOverflowLink;
import backend.academy.scrapper.service.ILinkService;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.Objects;
import backend.academy.scrapper.service.StackOverflowLinkService;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowProcessor extends Processor {

    private final StackOverflowClient stackOverflowClient;
    private final StackOverflowLinkService stackOverflowLinkService;

    public StackOverflowProcessor(StackOverflowClient stackOverflowClient, ILinkService linkService, StackOverflowLinkService stackOverflowLinkService) {
        super("stackoverflow.com", linkService);
        this.stackOverflowClient = stackOverflowClient;
        this.stackOverflowLinkService = stackOverflowLinkService;
    }

    @Override
    public String process(Link link) {
        StackOverflowResponseDto questionInfo = fetchQuestionInfo(link);

        StackOverflowAnswersListDto answersListDto = fetchAnswers(link);
        StackOverflowAnswerDto lastAnswer = answersListDto.answers().getFirst();

        StackOverflowCommentsListDto commentsListDto = fetchComments(link);
        StackOverflowCommentDto lastComment =  commentsListDto.comments().getFirst();
        System.out.println(lastAnswer);
        System.out.println(lastComment);
        StackOverflowLink relevantLink = new StackOverflowLink(
            lastAnswer.answerId(),
            lastAnswer.owner().displayName(),
            lastAnswer.createdAt().atOffset(ZoneOffset.UTC),
            lastAnswer.body(),

            lastComment.commendId(),
            lastComment.owner().displayName(),
            lastComment.createdAt().atOffset(ZoneOffset.UTC),
            lastComment.body()
        );
        relevantLink.id(link.id());
        System.out.println(relevantLink);
        if (isFirstTimeProcessing(link)) {
            System.out.println("First time processing " + link);
            touchLink(link);
            updateOrSave(relevantLink);
            return null;
        }
        touchLink(link);
        StackOverflowLink existedLink = stackOverflowLinkService.findById(link.id()).orElse(null);
        StringBuilder answer = new StringBuilder();

        if (!Objects.equals(existedLink.answerLastId(), lastAnswer.answerId())) {
            updateOrSave(relevantLink);
            answer.append("Появился новый ответ\n");
            answer.append("Автор:" + lastAnswer.owner().displayName() + "\n");
            answer.append("Дата создания:" + lastAnswer.createdAt() + "\n");
            answer.append("Описание: ").append(cutBody(lastAnswer.body())).append("\n");

            return answer.toString();
        }

        if (!Objects.equals(existedLink.commentId(), lastComment.commendId())) {
            updateOrSave(relevantLink);
            answer.append("Появился новый коментарий\n");
            answer.append("Автор:" + lastComment.owner().displayName() + "\n");
            answer.append("Дата создания:" + lastComment.createdAt() + "\n");


            answer.append("Описание: ").append(cutBody(lastComment.body())).append("\n");

            return answer.toString();
        }

        return null;
    }

    private void updateOrSave(StackOverflowLink link) {
        if (stackOverflowLinkService.findById(link.id()).isPresent()) {
            stackOverflowLinkService.updateLink(link);
        } else {
            stackOverflowLinkService.createLink(link);
        }
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
        assertSuccess(response, new RuntimeException("Cant fetch question comments : StackOverflow API is unavailable"));
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
