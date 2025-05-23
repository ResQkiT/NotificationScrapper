package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.clients.TelegramBotClient;
import backend.academy.scrapper.dto.LinkUpdate;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@WireMockTest(httpPort = 8089)
public class TelegramBotClientTest {

    private TelegramBotClient telegramBotClient;
    private DomainsConfig domainsConfig;

    @BeforeEach
    public void init() {
        domainsConfig = mock(DomainsConfig.class);
        when(domainsConfig.telegramBotUrl()).thenReturn("http://localhost:8089");
        RestClient.Builder builder = RestClient.builder();
        telegramBotClient = new TelegramBotClient(builder, domainsConfig);
    }

    @Test
    @DisplayName("Отправка обновления: при успешном запросе возвращается статус 200")
    public void testSendUpdate() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, "example.com", "test", List.of(123L, 456L));
        stubFor(post(urlEqualTo("/update"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(
                        equalToJson(
                                "{\"id\":1,\"url\":\"example.com\",\"description\":\"test\",\"tgChatIds\":[123,456]}")) // Исправлено поле
                .willReturn(aResponse().withStatus(200)));

        ResponseEntity<Void> response = telegramBotClient.sendUpdate(linkUpdate);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(
                postRequestedFor(urlEqualTo("/update"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withRequestBody(
                                equalToJson(
                                        "{\"id\":1,\"url\":\"example.com\",\"description\":\"test\",\"tgChatIds\":[123,456]}"))); // Исправлено поле
    }
}
