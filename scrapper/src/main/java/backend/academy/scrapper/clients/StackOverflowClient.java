package backend.academy.scrapper.clients;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowAnswersListDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowCommentsListDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class StackOverflowClient extends Client {
    private static final String SITE = "stackoverflow";
    private static final String QUESTION_FILTER = "!9Z(-wzu0T"; // Включает body вопроса
    private static final String ANSWER_FILTER = "withbody";    // Включает body ответов
    private static final String COMMENT_FILTER = "withbody";      // Включает body комментариев

    private final String API_KEY;
    private final String ACCESS_TOKEN;
    private final String baseUrl;

    @Autowired
    public StackOverflowClient(
        RestClient.Builder restClientBuilder, ScrapperConfig scrapperConfig, DomainsConfig domainsConfig) {
        super(restClientBuilder.build());
        this.API_KEY = scrapperConfig.stackOverflow().key();
        this.ACCESS_TOKEN = scrapperConfig.stackOverflow().accessToken();
        this.baseUrl = domainsConfig.stackoverflow();
    }

    public ResponseEntity<StackOverflowResponseDto> getQuestionInfo(Long questionId) {
        return client().get()
            .uri(baseUrl + "/questions/{id}",
                uriBuilder -> uriBuilder
                    .queryParam("site", SITE)
                    .queryParam("filter", QUESTION_FILTER)
                    .queryParam("key", API_KEY)
                    .queryParam("access_token", ACCESS_TOKEN)
                .build(questionId))
            .header("Accept", "application/json")
            .retrieve()
            .toEntity(StackOverflowResponseDto.class);
    }

    public ResponseEntity<StackOverflowAnswersListDto> getQuestionAnswers(Long questionId) {
        return client().get()
            .uri(baseUrl + "/questions/{id}/answers",
                uriBuilder -> uriBuilder
                    .queryParam("site", SITE)
                    .queryParam("filter", ANSWER_FILTER)
                    .queryParam("order", "desc")
                    .queryParam("sort", "creation")
                    .queryParam("key", API_KEY)
                    .queryParam("access_token", ACCESS_TOKEN)
                .build(questionId))
            .header("Accept", "application/json")
            .retrieve()
            .toEntity(StackOverflowAnswersListDto.class);
    }

    public ResponseEntity<StackOverflowCommentsListDto> getQuestionComments(Long questionId) {
        return client().get()
            .uri(baseUrl + "/questions/{id}/comments",
                uriBuilder -> uriBuilder
                    .queryParam("site", SITE)
                    .queryParam("filter", COMMENT_FILTER)
                    .queryParam("order", "desc")
                    .queryParam("sort", "creation")
                    .queryParam("key", API_KEY)
                    .queryParam("access_token", ACCESS_TOKEN)
                .build(questionId))
            .header("Accept", "application/json")
            .retrieve()
            .toEntity(StackOverflowCommentsListDto.class);
    }
}
