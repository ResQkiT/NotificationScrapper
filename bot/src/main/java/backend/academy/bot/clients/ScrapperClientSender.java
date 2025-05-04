package backend.academy.bot.clients;

import backend.academy.bot.config.DomainsConfig;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class ScrapperClientSender implements IClient {
    private final RestClient restClient;
    private final String SCHEDULER_SERVICE_URL;
    private static final String TG_ID_HEADER = "Tg-Chat-Id";

    public ScrapperClientSender(RestClient.Builder restClientBuilder, DomainsConfig domainsConfig) {
        this.SCHEDULER_SERVICE_URL = domainsConfig.scrapper();
        this.restClient = restClientBuilder.baseUrl(SCHEDULER_SERVICE_URL).build();
    }

    @Override
    @Retry(name = "scrapperRetry", fallbackMethod = "fallbackRegisterChat")
    @CircuitBreaker(name = "scrapperCircuitBreaker")
    @RateLimiter(name = "scrapperRateLimiter")
    public ResponseEntity<Void> registerChat(Long id) {
        log.info("Registering chat with id: {}", id);
        return restClient.post().uri("/tg-chat/{id}", id).retrieve().toBodilessEntity();
    }

    public ResponseEntity<Void> fallbackRegisterChat(Long id, Throwable t) {
        log.warn("Fallback for registerChat called: {}", t.getMessage(), t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    @Retry(name = "scrapperRetry", fallbackMethod = "fallbackDeleteChat")
    @CircuitBreaker(name = "scrapperCircuitBreaker")
    @RateLimiter(name = "scrapperRateLimiter")
    public ResponseEntity<Void> deleteChat(Long id) {
        log.info("Deleting chat with id: {}", id);
        return restClient.delete().uri("/tg-chat/{id}", id).retrieve().toBodilessEntity();
    }

    public ResponseEntity<Void> fallbackDeleteChat(Long id, Throwable t) {
        log.warn("Fallback for deleteChat called: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    @Retry(name = "scrapperRetry", fallbackMethod = "fallbackGetTrackedLinks")
    @CircuitBreaker(name = "scrapperCircuitBreaker")
    @RateLimiter(name = "scrapperRateLimiter")
    public ResponseEntity<ListLinksResponse> getTrackedLinks(Long chatId) {
        log.info("Fetching tracked links for chatId: {}", chatId);
        return restClient
                .get()
                .uri("/links")
                .header(TG_ID_HEADER, chatId.toString())
                .retrieve()
                .toEntity(ListLinksResponse.class);
    }

    public ResponseEntity<ListLinksResponse> fallbackGetTrackedLinks(Long chatId, Throwable t) {
        log.warn("Fallback for getTrackedLinks called: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ListLinksResponse(List.of(), 0));
    }

    @Override
    @Retry(name = "scrapperRetry", fallbackMethod = "fallbackAddLink")
    @CircuitBreaker(name = "scrapperCircuitBreaker")
    @RateLimiter(name = "scrapperRateLimiter")
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

    public ResponseEntity<LinkResponse> fallbackAddLink(Long chatId, AddLinkRequest request, Throwable t) {

        log.warn("Fallback for addLink called: {}", t.getMessage(), t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    @Retry(name = "scrapperRetry", fallbackMethod = "fallbackRemoveLink")
    @CircuitBreaker(name = "scrapperCircuitBreaker")
    @RateLimiter(name = "scrapperRateLimiter")
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

    public ResponseEntity<LinkResponse> fallbackRemoveLink(Long chatId, RemoveLinkRequest request, Throwable t) {
        log.warn("Fallback for removeLink called: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
