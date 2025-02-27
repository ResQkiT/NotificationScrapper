package backend.academy.bot.clients;

import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import backend.academy.bot.dto.ListLinksResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@Component
public class ScrapperClient {

    private final RestClient restClient;
    private static final String SCHEDULER_SERVICE_URL = "http://localhost:8081";
    private static final String TG_ID_HEADER = "Tg-Chat-Id";

    @Autowired
    public ScrapperClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
            .baseUrl(SCHEDULER_SERVICE_URL)
            .build();
    }

    public ResponseEntity<Void> registerChat(Long id) {
        try {
            log.info("Registering chat with id: {}", id);
            ResponseEntity<Void> response = restClient
                .post()
                .uri("/tg-chat/{id}", id)
                .retrieve()
                .toBodilessEntity();
            log.info("Register chat response: {}", response);
            return response;
        }catch (HttpClientErrorException e) {
            log.error("Error registering chat: Status={} Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    public ResponseEntity<Void> deleteChat(Long id) {
        try {
            log.info("Deleting chat with id: {}", id);
            ResponseEntity<Void> response = restClient
                .delete()
                .uri("/tg-chat/{id}", id)
                .retrieve()
                .toBodilessEntity();
            log.info("Delete chat response: {}", response);
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Error deleting chat: Status={} Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    public ResponseEntity<ListLinksResponse> getTrackedLinks(Long chatId) {
        try {
            log.info("Fetching tracked links for chatId: {}", chatId);
            return restClient
                .get()
                .uri("/links")
                .header(TG_ID_HEADER, chatId.toString())
                .retrieve()
                .toEntity(ListLinksResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("Error fetching tracked links: Status={} Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(new ListLinksResponse(List.of(), 0));
        } catch (Exception e) {
            log.error("Unexpected error while fetching tracked links", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ListLinksResponse(List.of(), 0));
        }
    }

    public ResponseEntity<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        try {
            log.info("Adding link for chatId: {} with request: {}", chatId, request);
            ResponseEntity<LinkResponse> response = restClient
                .post()
                .uri("/links")
                .header(TG_ID_HEADER, chatId.toString())
                .body(request)
                .retrieve()
                .toEntity(LinkResponse.class);
            log.info("Add link response: {}", response);
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Error adding link: Status={} Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    public ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request) {
        try {
            log.info("Removing link for chatId: {} with request: {}", chatId, request);
            ResponseEntity<LinkResponse> response = restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", chatId.toString())
                .body(request)
                .retrieve()
                .toEntity(LinkResponse.class);
            log.info("Remove link response: {}", response);
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Error removing link: Status={} Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }
}
