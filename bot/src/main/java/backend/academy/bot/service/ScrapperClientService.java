package backend.academy.bot.service;

import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import backend.academy.bot.dto.ListLinksResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ScrapperClientService {

    private final RestClient restClient;
    private static final String BASE_URL = "https://localhost:8081";

    @Autowired
    public ScrapperClientService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
            .baseUrl(BASE_URL)
            .build();
    }

    public ResponseEntity<Void> registerChat(Long id) {
        return restClient
            .post()
            .uri("/tg-chat/{id}", id)
            .retrieve()
            .toBodilessEntity();
    }

    public ResponseEntity<Void> deleteChat(Long id) {
        return restClient
            .delete()
            .uri("/tg-chat/{id}", id)
            .retrieve()
            .toBodilessEntity();
    }

    public ListLinksResponse getTrackedLinks(Long chatId) {
        return restClient
            .get()
            .uri("/links")
            .header("Tg-Chat-Id", chatId.toString())
            .retrieve()
            .body(ListLinksResponse.class);
    }

    public ResponseEntity<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        return restClient
            .post()
            .uri("/links")
            .header("Tg-Chat-Id", chatId.toString())
            .body(request)
            .retrieve()
            .toEntity(LinkResponse.class);
    }


    public ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request) {
        return restClient
            .method(HttpMethod.DELETE)
            .uri("/links")
            .header("Tg-Chat-Id", chatId.toString())
            .body(request)
            .retrieve()
            .toEntity(LinkResponse.class);
    }


}
