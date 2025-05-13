package backend.academy.bot.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@WireMockTest(httpPort = 8081)
@SpringBootTest
@TestPropertySource(properties = "spring.caching=false")
class ScrapperClientTest {

    @Autowired
    private ScrapperClient scrapperClient;

    @Test
    @DisplayName("Регистрация чата: при успешном запросе возвращается статус OK")
    void testRegister_whenRequestIsOk_thenReturnOk() {
        stubFor(post("/tg-chat/123").willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        ResponseEntity<Void> response = scrapperClient.registerChat(123L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(postRequestedFor(urlEqualTo("/tg-chat/123")));
    }

    @Test
    @DisplayName("Удаление чата: при успешном запросе возвращается статус OK")
    void testDeleteChat_whenRequestIsOk_thenReturnOk() {
        stubFor(delete("/tg-chat/123").willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        ResponseEntity<Void> response = scrapperClient.deleteChat(123L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(deleteRequestedFor(urlEqualTo("/tg-chat/123")));
    }

    @Test
    @DisplayName("Получение ссылок: если у чата нет ссылок, возвращается корректный ответ")
    void testGetLinks_whenChatHasNoLinks_thenReturnValidResponse() {
        stubFor(get("/links")
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"links\": [], \"size\": 0}")));

        ResponseEntity<ListLinksResponse> response = scrapperClient.getTrackedLinks(123L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().links()).isNullOrEmpty();
        assertThat(response.getBody().size()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Получение ссылок: если у чата есть ссылки, возвращается корректный ответ")
    void testGetLinks_whenChatHasLinks_thenReturnValidResponse() {
        stubFor(get("/links")
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"links\":[{\"id\":1," + "                   \"url\":\"http://example.com\",  "
                                + "                   \"tags\": [\"t1\", \"t2\"],"
                                + "                   \"filters\": [\"f1\", \"f2\"]}],"
                                + "               \"size\":1}")));

        ResponseEntity<ListLinksResponse> response = scrapperClient.getTrackedLinks(123L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(1);
        assertThat(response.getBody().links()).isNotEmpty();
        assertThat(response.getBody().links().getFirst().url()).isEqualTo("http://example.com");
        assertThat(response.getBody().links().getFirst().tags()).containsExactly("t1", "t2");
        assertThat(response.getBody().links().getFirst().filters()).containsExactly("f1", "f2");

        verify(getRequestedFor(urlEqualTo("/links")).withHeader("Tg-Chat-Id", equalTo("123")));
    }

    @Test
    @DisplayName("Добавление ссылки: при успешном запросе возвращается статус OK")
    void testAddLink_whenRequestOsOk_thenResponseIsOk() {
        AddLinkRequest request =
                new AddLinkRequest("http://example.com", Arrays.asList("t1", "t2"), Arrays.asList("f1", "f2"));

        stubFor(post("/links")
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("                 {\"id\":1," + "                   \"url\":\"http://example.com\",  "
                                + "                   \"tags\": [\"t1\", \"t2\"],"
                                + "                   \"filters\": [\"f1\", \"f2\"]}],"
                                + "               \"size\":1}")));

        ResponseEntity<LinkResponse> response = scrapperClient.addLink(123L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().url()).isEqualTo("http://example.com");
        assertThat(response.getBody().tags()).containsExactly("t1", "t2");
        assertThat(response.getBody().filters()).containsExactly("f1", "f2");

        verify(postRequestedFor(urlEqualTo("/links")).withHeader("Tg-Chat-Id", equalTo("123")));
    }

    @Test
    @DisplayName("Удаление ссылки: при успешном запросе возвращается статус OK")
    void testRemoveLink_whenRequestIsOk_thenResponseIsOk() {
        RemoveLinkRequest request = new RemoveLinkRequest("http://example.com");

        stubFor(delete("/links")
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        ResponseEntity<LinkResponse> response = scrapperClient.removeLink(123L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(deleteRequestedFor(urlEqualTo("/links")).withHeader("Tg-Chat-Id", equalTo("123")));
    }
}
