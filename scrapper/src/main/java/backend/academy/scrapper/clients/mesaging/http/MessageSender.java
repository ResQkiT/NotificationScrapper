package backend.academy.scrapper.clients.mesaging.http;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.dto.LinkUpdate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class MessageSender {
    private final RestClient restClient;

    @Autowired
    public MessageSender(RestClient.Builder restClientBuilder, DomainsConfig domainsConfig) {
        this.restClient =
            restClientBuilder.baseUrl(domainsConfig.telegramBotUrl()).build();
    }

    @RateLimiter(name = "telegramRateLimiter")
    @Retry(name = "telegramRetry")
    @CircuitBreaker(name = "telegramCircuitBreaker", fallbackMethod = "fallbackSendMessage")
    public ResponseEntity<Void> sendMessage(LinkUpdate linkUpdate) {
        log.info("Sending link update {}", linkUpdate);
        return restClient
            .post()
            .uri("/update")
            .contentType(MediaType.APPLICATION_JSON)
            .body(linkUpdate)
            .retrieve()
            .toBodilessEntity();
    }

    public ResponseEntity<Void> fallbackSendMessage(LinkUpdate linkUpdate, Throwable t) {
        log.warn("Fallback method called: " + t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
