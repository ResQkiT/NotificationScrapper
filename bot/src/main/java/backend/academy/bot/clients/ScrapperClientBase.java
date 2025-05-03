package backend.academy.bot.clients;

import backend.academy.bot.config.DomainsConfig;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class ScrapperClientBase {
    private final RestClient restClient;
    private final String SCHEDULER_SERVICE_URL;
    private static final String TG_ID_HEADER = "Tg-Chat-Id";

    public ScrapperClientBase(RestClient.Builder restClientBuilder, DomainsConfig domainsConfig) {
        this.SCHEDULER_SERVICE_URL = domainsConfig.scrapper();
        this.restClient = restClientBuilder.baseUrl(SCHEDULER_SERVICE_URL).build();
    }

    public ResponseEntity<Void> registerChat(Long id) {
        log.info("Registering chat with id: {}", id);
        log.info("Class type: {}", this.getClass().getName());
        return restClient.post().uri("/tg-chat/{id}", id).retrieve().toBodilessEntity();
    }

    public ResponseEntity<Void> deleteChat(Long id) {
        log.info("Deleting chat with id: {}", id);
        return restClient.delete().uri("/tg-chat/{id}", id).retrieve().toBodilessEntity();
    }

    public ResponseEntity<ListLinksResponse> getTrackedLinks(Long chatId) {
        log.info("Fetching tracked links for chatId: {}", chatId);
        return restClient
                .get()
                .uri("/links")
                .header(TG_ID_HEADER, chatId.toString())
                .retrieve()
                .toEntity(ListLinksResponse.class);

    }

    public ResponseEntity<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        log.info("Adding link for chatId: {} with request: {}", chatId, request);
        return restClient
                .post()
                .uri("/links")
                .header(TG_ID_HEADER, chatId.toString())
                .body(request)
                .retrieve()
                .toEntity(LinkResponse.class);
    }

    public ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request) {
        log.info("Removing link for chatId: {} with request: {}", chatId, request);
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", chatId.toString())
                .body(request)
                .retrieve()
                .toEntity(LinkResponse.class);
    }
}
