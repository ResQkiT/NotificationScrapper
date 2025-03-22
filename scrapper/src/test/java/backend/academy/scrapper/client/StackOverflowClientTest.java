package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.dto.stackoverflow.StackOverflowResponseDto;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@WireMockTest(httpPort = 8089)
public class StackOverflowClientTest {
    private StackOverflowClient stackOverflowClient;

    @BeforeEach
    void setUp() {
        ScrapperConfig scrapperConfig = mock(ScrapperConfig.class);
        ScrapperConfig.StackOverflowCredentials credentials = mock(ScrapperConfig.StackOverflowCredentials.class);

        when(scrapperConfig.stackOverflow()).thenReturn(credentials);

        when(credentials.key()).thenReturn("test-key");
        when(credentials.accessToken()).thenReturn("test-access-token");

        DomainsConfig domainsConfig = new DomainsConfig("", "http://localhost:8089", "", "");

        RestClient.Builder restClientBuilder = RestClient.builder();
        stackOverflowClient = new StackOverflowClient(restClientBuilder, scrapperConfig, domainsConfig);
    }

    @Test
    @DisplayName("Получение информации о вопросе: при успешном запросе возвращаются данные вопроса")
    void getQuestionInfo_shouldReturnQuestionData() {
        Long questionId = 12345L;

        stubFor(get(urlPathEqualTo("/questions/" + questionId))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("filter", equalTo("!9Z(-wzu0T"))
                .withQueryParam("key", equalTo("test-key"))
                .withQueryParam("access_token", equalTo("test-access-token"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{" + "\"items\": [{"
                                + "\"title\": \"Sample Question\","
                                + "\"last_activity_date\": \"2024-01-01T12:00:00Z\","
                                + "\"answer_count\": 5,"
                                + "\"score\": 10"
                                + "}]}")));

        ResponseEntity<StackOverflowResponseDto> response = stackOverflowClient.getQuestionInfo(questionId);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().items()).hasSize(1);

        StackOverflowResponseDto.Question question = response.getBody().items().get(0);
        assertThat(question.title()).isEqualTo("Sample Question");
        assertThat(question.lastActivityDate()).isEqualTo(OffsetDateTime.parse("2024-01-01T12:00:00Z"));
        assertThat(question.answerCount()).isEqualTo(5);
        assertThat(question.score()).isEqualTo(10);
    }
}
