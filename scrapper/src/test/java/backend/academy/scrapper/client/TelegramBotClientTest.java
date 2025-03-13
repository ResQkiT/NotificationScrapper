package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.clients.TelegramBotClient;
import backend.academy.scrapper.dto.LinkUpdate;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

public class TelegramBotClientTest {

    private static WireMockServer wireMockServer;
    private TelegramBotClient telegramBotClient;
    private DomainsConfig domainsConfig;

    @BeforeAll
    public static void setup() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
    }

    @AfterAll
    public static void teardown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    public void init() {
        wireMockServer.resetAll();
        domainsConfig = mock(DomainsConfig.class);
        when(domainsConfig.telegramBotUrl()).thenReturn("http://localhost:8080");
        RestClient.Builder builder = RestClient.builder();
        telegramBotClient = new TelegramBotClient(builder, domainsConfig);
    }

    @Test
    @DisplayName("Отправка обновления: при успешном запросе возвращается статус 200")
    public void testSendUpdate() {
        LinkUpdate linkUpdate = new LinkUpdate(1L, "example.com", "test", List.of(123L, 456L));
        stubFor(post(urlEqualTo("/update"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200)));

        ResponseEntity<Void> response = telegramBotClient.sendUpdate(linkUpdate);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(postRequestedFor(urlEqualTo("/update")).withHeader("Content-Type", equalTo("application/json")));
    }
}
