package backend.academy.bot.clients;

import backend.academy.bot.config.DomainsConfig;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import java.util.List;
import java.util.Set;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
@Primary
@ConditionalOnProperty(name = "spring.caching", havingValue = "true")
public class CachedScrapperClient extends ScrapperClientBase implements IClient {

    private final RedisTemplate<Long, LinkResponse> redisTemplate;

    @Autowired
    public CachedScrapperClient(
            RestClient.Builder restClientBuilder,
            DomainsConfig domainsConfig,
            RedisTemplate<Long, LinkResponse> redisTemplate) {
        super(restClientBuilder, domainsConfig);
        this.redisTemplate = redisTemplate;
    }

    @Override
    @RateLimiter(name = "scrapperRateLimiter")
    @Retry(name = "scrapperRetry")
    @CircuitBreaker(name = "scrapperCircuitBreaker", fallbackMethod = "fallbackRegisterChat")
    public ResponseEntity<Void> registerChat(Long id) {
        log.info("Class type: {}", this.getClass().getName());
        return super.registerChat(id);
    }

    public ResponseEntity<Void> fallbackRegisterChat(Long id, Throwable t) {
        log.warn("Fallback for registerChat called: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    @RateLimiter(name = "scrapperRateLimiter")
    @Retry(name = "scrapperRetry")
    @CircuitBreaker(name = "scrapperCircuitBreaker", fallbackMethod = "fallbackDeleteChat")
    public ResponseEntity<Void> deleteChat(Long id) {
        return super.deleteChat(id);
    }

    public ResponseEntity<Void> fallbackDeleteChat(Long id, Throwable t) {
        log.warn("Fallback for deleteChat called: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    @RateLimiter(name = "scrapperRateLimiter")
    @Retry(name = "scrapperRetry")
    @CircuitBreaker(name = "scrapperCircuitBreaker", fallbackMethod = "fallbackGetTrackedLinks")
    public ResponseEntity<ListLinksResponse> getTrackedLinks(Long chatId) {
        if (redisTemplate.hasKey(chatId)) {
            log.info("Get Tracked Links fom Redis");
            Set<LinkResponse> linkResponses = redisTemplate.opsForSet().members(chatId);
            ListLinksResponse response =
                    new ListLinksResponse(linkResponses.stream().toList(), linkResponses.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        log.info("Fetch Tracked Links");
        ResponseEntity<ListLinksResponse> response = super.getTrackedLinks(chatId);
        if (response.getBody().size() > 0) {
            redisTemplate.opsForSet().add(chatId, response.getBody().links().toArray(new LinkResponse[0]));
        } else {
            redisTemplate.delete(chatId);
        }
        return response;
    }

    public ResponseEntity<ListLinksResponse> fallbackGetTrackedLinks(Long chatId, Throwable t) {
        log.warn("Fallback for getTrackedLinks called: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ListLinksResponse(List.of(), 0));
    }

    @Override
    @RateLimiter(name = "scrapperRateLimiter")
    @Retry(name = "scrapperRetry")
    @CircuitBreaker(name = "scrapperCircuitBreaker", fallbackMethod = "fallbackAddLink")
    public ResponseEntity<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        ResponseEntity<LinkResponse> response = super.addLink(chatId, request);
        log.info("Add Link");
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Link added successfully, invalidating Redis cache for chat {}", chatId);
            redisTemplate.delete(chatId);
        }
        return response;
    }

    public ResponseEntity<LinkResponse> fallbackAddLink(Long chatId, AddLinkRequest request, Throwable t) {
        log.warn("Fallback for addLink called: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    @RateLimiter(name = "scrapperRateLimiter")
    @Retry(name = "scrapperRetry")
    @CircuitBreaker(name = "scrapperCircuitBreaker", fallbackMethod = "fallbackRemoveLink")
    public ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request) {
        ResponseEntity<LinkResponse> response = super.removeLink(chatId, request);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Link removed successfully, invalidating Redis cache for chat {}", chatId);
            redisTemplate.delete(chatId);
        }
        return response;
    }
    public ResponseEntity<LinkResponse> fallbackRemoveLink(Long chatId, RemoveLinkRequest request, Throwable t) {
        log.warn("Fallback for removeLink called: {}", t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
