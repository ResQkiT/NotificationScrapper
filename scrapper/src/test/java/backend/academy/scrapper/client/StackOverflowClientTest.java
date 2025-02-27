package backend.academy.scrapper.client;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.dto.StackOverflowResponseDto;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WireMockTest(httpPort = 8089)
public class StackOverflowClientTest {
    private StackOverflowClient stackOverflowClient;

    @BeforeEach
    void setUp() {
        // Создаем объект StackOverflowCredentials с тестовыми данными
        ScrapperConfig scrapperConfig = mock(ScrapperConfig.class);
        ScrapperConfig.StackOverflowCredentials credentials = mock(ScrapperConfig.StackOverflowCredentials.class);

        // Мокаем метод stackOverflow(), чтобы он возвращал объект credentials
        when(scrapperConfig.stackOverflow()).thenReturn(credentials);

        // Теперь мокаем методы key() и accessToken() на объекте credentials
        when(credentials.key()).thenReturn("test-key");
        when(credentials.accessToken()).thenReturn("test-access-token");

        // Конфигурируем DomainsConfig
        DomainsConfig domainsConfig = new DomainsConfig();
        domainsConfig.stackoverflow("http://localhost:8089");

        // Настроим RestClient.Builder и StackOverflowClient
        RestClient.Builder restClientBuilder = RestClient.builder();
        stackOverflowClient = new StackOverflowClient(restClientBuilder, scrapperConfig, domainsConfig);
    }

    @Test
    void getQuestionInfo_shouldReturnQuestionData() {
        // Arrange
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
                .withBody("{" +
                    "\"items\": [{" +
                    "\"title\": \"Sample Question\"," +
                    "\"last_activity_date\": \"2024-01-01T12:00:00Z\"," +
                    "\"answer_count\": 5," +
                    "\"score\": 10" +
                    "}]}")));

        // Act
        ResponseEntity<StackOverflowResponseDto> response = stackOverflowClient.getQuestionInfo(questionId);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().items()).hasSize(1);

        StackOverflowResponseDto.Question question = response.getBody().items().get(0);
        assertThat(question.title()).isEqualTo("Sample Question");
        assertThat(question.lastActivityDate()).isEqualTo(OffsetDateTime.parse("2024-01-01T12:00:00Z"));
        assertThat(question.answerCount()).isEqualTo(5);
        assertThat(question.score()).isEqualTo(10);
    }
}
