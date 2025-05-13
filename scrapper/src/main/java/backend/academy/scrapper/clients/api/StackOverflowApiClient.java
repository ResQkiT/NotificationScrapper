package backend.academy.scrapper.clients.api;

import backend.academy.scrapper.config.DomainsConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowAnswersListDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowCommentsListDto;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class StackOverflowApiClient extends ApiClient {
    private static final String SITE = "stackoverflow";
    private static final String QUESTION_FILTER = "!9Z(-wzu0T";
    private static final String ANSWER_FILTER = "withbody";
    private static final String COMMENT_FILTER = "withbody";

    private final String API_KEY;
    private final String ACCESS_TOKEN;
    private final String baseUrl;

    @Autowired
    public StackOverflowApiClient(
            RestClient.Builder restClientBuilder, ScrapperConfig scrapperConfig, DomainsConfig domainsConfig) {
        super(restClientBuilder.build());
        this.API_KEY = scrapperConfig.stackOverflow().key();
        this.ACCESS_TOKEN = scrapperConfig.stackOverflow().accessToken();
        this.baseUrl = domainsConfig.stackoverflow();
    }

    public ResponseEntity<StackOverflowResponseDto> getQuestionInfo(Long questionId) {
        return client().get()
                .uri(baseUrl + "/questions/{id}", uriBuilder -> uriBuilder
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
                .uri(baseUrl + "/questions/{id}/answers", uriBuilder -> uriBuilder
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
                .uri(baseUrl + "/questions/{id}/comments", uriBuilder -> uriBuilder
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
