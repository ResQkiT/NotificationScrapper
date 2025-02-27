package backend.academy.scrapper.clients;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.StackOverflowResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class StackOverflowClient extends Client {
    private static final String SITE = "stackoverflow";
    private static final String FILTER = "!9Z(-wzu0T";

    private final String API_KEY;
    private final String ACCESS_TOKEN;

    @Autowired
    public StackOverflowClient(RestClient.Builder restClientBuilder, ScrapperConfig scrapperConfig, DomainsConfig domainsConfig) {
        super(restClientBuilder
            .baseUrl(domainsConfig.stackoverflow() + "/questions/{id}")
            .build());
        this.API_KEY = scrapperConfig.stackOverflow().key();
        this.ACCESS_TOKEN = scrapperConfig.stackOverflow().accessToken();
    }

    public ResponseEntity<StackOverflowResponseDto> getQuestionInfo(Long questionId) {
        return client().get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("site", SITE)
                .queryParam("filter", FILTER)
                .queryParam("key", API_KEY)
                .queryParam("access_token", ACCESS_TOKEN)
                .build(questionId))
            .header("Accept", "application/json")
            .retrieve()
            .toEntity(StackOverflowResponseDto.class);
    }

}
