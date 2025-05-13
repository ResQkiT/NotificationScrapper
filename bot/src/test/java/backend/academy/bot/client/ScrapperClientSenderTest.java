package backend.academy.bot.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.bot.clients.ScrapperClientSender;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.RemoveLinkRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "domains.scrapper=http://localhost:8081")
public class ScrapperClientSenderTest {
    @Autowired
    private ScrapperClientSender client;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(options().port(8081));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
    }

    @BeforeEach
    public void resetCircuitBreaker() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("scrapperCircuitBreaker");
        circuitBreaker.reset();
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    public void testRegisterChatWithRetries() {
        wireMockServer.stubFor(post(urlEqualTo("/tg-chat/1"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("FirstFailure"));

        wireMockServer.stubFor(post(urlEqualTo("/tg-chat/1"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("FirstFailure")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("SecondFailure"));

        wireMockServer.stubFor(post(urlEqualTo("/tg-chat/1"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("SecondFailure")
                .willReturn(aResponse().withStatus(200)));

        ResponseEntity<Void> response = client.registerChat(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testRegisterChatRetryExhausted() {
        wireMockServer.stubFor(
                post(urlEqualTo("/tg-chat/1")).willReturn(aResponse().withStatus(500)));

        ResponseEntity<Void> response = client.registerChat(1L);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    public void testCircuitBreakerOnRegisterChat() {
        wireMockServer.stubFor(
                post(urlEqualTo("/tg-chat/1")).willReturn(aResponse().withStatus(500)));

        ResponseEntity<Void> response = null;
        for (int i = 0; i < 5; i++) {
            response = client.registerChat(1L);
            if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                break;
            }
        }

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    public void testDeleteChatWithRetries() {
        wireMockServer.stubFor(delete(urlEqualTo("/tg-chat/1"))
                .inScenario("DeleteRetryScenario")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("FirstFailure"));

        wireMockServer.stubFor(delete(urlEqualTo("/tg-chat/1"))
                .inScenario("DeleteRetryScenario")
                .whenScenarioStateIs("FirstFailure")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("SecondFailure"));

        wireMockServer.stubFor(delete(urlEqualTo("/tg-chat/1"))
                .inScenario("DeleteRetryScenario")
                .whenScenarioStateIs("SecondFailure")
                .willReturn(aResponse().withStatus(200)));

        ResponseEntity<Void> response = client.deleteChat(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testAddLinkWithRetries() {
        wireMockServer.stubFor(post(urlEqualTo("/links"))
                .inScenario("AddLinkRetryScenario")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("FirstFailure"));

        wireMockServer.stubFor(post(urlEqualTo("/links"))
                .inScenario("AddLinkRetryScenario")
                .whenScenarioStateIs("FirstFailure")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("SecondFailure"));

        wireMockServer.stubFor(
                post(urlEqualTo("/links"))
                        .inScenario("AddLinkRetryScenario")
                        .whenScenarioStateIs("SecondFailure")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
            {
                "id": 1,
                "url": "http://example.com",
                "tags": [],
                "filters": []
            }
            """)));
        AddLinkRequest request = new AddLinkRequest("http://example.com", List.of(), List.of());
        ResponseEntity<?> response = client.addLink(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testRemoveLinkWithRetries() {
        wireMockServer.stubFor(delete(urlEqualTo("/links"))
                .inScenario("RemoveLinkRetryScenario")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("FirstFailure"));

        wireMockServer.stubFor(delete(urlEqualTo("/links"))
                .inScenario("RemoveLinkRetryScenario")
                .whenScenarioStateIs("FirstFailure")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("SecondFailure"));

        wireMockServer.stubFor(
                delete(urlEqualTo("/links"))
                        .inScenario("RemoveLinkRetryScenario")
                        .whenScenarioStateIs("SecondFailure")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
            {
                "id": 1,
                "url": "http://example.com",
                "tags": [],
                "filters": []
            }
            """)));
        RemoveLinkRequest request = new RemoveLinkRequest("http://example.com");
        ResponseEntity<?> response = client.removeLink(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
