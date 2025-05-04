package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.clients.mesaging.http.HttpMessageSender;
import backend.academy.scrapper.config.DomainsConfig;
import backend.academy.scrapper.dto.LinkUpdate;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@WireMockTest(httpPort = 8089)
public class TelegramBotApiClientTest {

    private HttpMessageSender telegramBotClient;
    private DomainsConfig domainsConfig;

    @BeforeEach
    public void init() {
        domainsConfig = mock(DomainsConfig.class);
        when(domainsConfig.telegramBotUrl()).thenReturn("http://localhost:8089");
        RestClient.Builder builder = RestClient.builder();
        telegramBotClient = new HttpMessageSender(builder, domainsConfig);
    }

    @Test
    @DisplayName("Отправка обновления: при успешном запросе возвращается статус 200")
    public void testSendUpdate() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, "example.com", "test", List.of(123L, 456L));
        stubFor(post(urlEqualTo("/update"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(
                        "{\"id\":1,\"url\":\"example.com\",\"description\":\"test\",\"tgChatIds\":[123,456]}"))
                .willReturn(aResponse().withStatus(200)));

        ResponseEntity<Void> response = telegramBotClient.sendMessage(linkUpdate);

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
