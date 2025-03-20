package backend.academy.scrapper.processor;

import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.dto.StackOverflowResponseDto;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.ILinkService;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowProcessor extends Processor {

    private final StackOverflowClient stackOverflowClient;

    public StackOverflowProcessor(StackOverflowClient stackOverflowClient, ILinkService linkService) {
        super("stackoverflow.com", linkService);
        this.stackOverflowClient = stackOverflowClient;
    }

    @Override
    public String process(Link link) {
        StackOverflowResponseDto questionInfo = fetchQuestionInfo(link);

        var lastQuestion = questionInfo.items().getFirst();

        if (isFirstTimeProcessing(link)) {
            updateLink(link, lastQuestion.lastActivityDate());
            return null;
        }

        if (hasUpdates(lastQuestion.lastActivityDate(), link)) {
            updateLink(link, lastQuestion.lastActivityDate());
            return "Есть изменения";
        }

        return null;
    }

    private StackOverflowResponseDto fetchQuestionInfo(Link link) {
        URI uri = URI.create(link.url());

        Long questionId = extractQuestionId(uri.getPath());
        var response = stackOverflowClient.getQuestionInfo(questionId);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("StackOverflow API is unavailable");
        }

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
