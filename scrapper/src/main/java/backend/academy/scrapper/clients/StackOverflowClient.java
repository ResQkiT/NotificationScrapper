package backend.academy.scrapper.clients;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.StackOverflowQuestionResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class StackOverflowClient extends Client {
    private static final String STACKOVERFLOW_BASE_API_URL = "https://api.stackexchange.com/2.3/questions/{id}";
    private static final String SITE = "ru.stackoverflow";
    private static final String FILTER = "!9Z(-wzu0T"; //дата последнего изменения

    private final String API_KEY;
    private final String ACCESS_TOKEN;

    @Autowired
    public StackOverflowClient(RestClient.Builder restClientBuilder, ScrapperConfig scrapperConfig) {
        super(restClientBuilder
            .baseUrl(STACKOVERFLOW_BASE_API_URL)
            .build());

        this.API_KEY = scrapperConfig.stackOverflow().key();
        this.ACCESS_TOKEN = scrapperConfig.stackOverflow().key();
    }

    public ResponseEntity<StackOverflowQuestionResponseDto> getQuestionInfo(long questionId) {
        return client().get()
            .uri(uriBuilder -> uriBuilder
                .path("/")
                .queryParam("site", SITE)
                .queryParam("filter", FILTER)
                .queryParam("key", API_KEY)
                .queryParam("access_token", ACCESS_TOKEN)
                .build(questionId))
            .header("Accept", "application/json")
            .retrieve()
            .toEntity(StackOverflowQuestionResponseDto.class);
    }

}
