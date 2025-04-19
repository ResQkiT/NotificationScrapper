package backend.academy.bot.clients;

import backend.academy.bot.config.DomainsConfig;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import java.util.Set;
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
@ConditionalOnProperty(name = "spring.caching", havingValue = "true")
@Primary
public class CachedScrapperClient extends ScrapperClient implements IClient {

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
    public ResponseEntity<Void> registerChat(Long id) {
        return super.registerChat(id);
    }

    @Override
    public ResponseEntity<Void> deleteChat(Long id) {
        return super.deleteChat(id);
    }

    @Override
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

    @Override
    public ResponseEntity<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        ResponseEntity<LinkResponse> response = super.addLink(chatId, request);
        log.info("Add Link");
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Link added successfully, invalidating Redis cache for chat {}", chatId);
            redisTemplate.delete(chatId);
        }
        return response;
    }

    @Override
    public ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request) {
        ResponseEntity<LinkResponse> response = super.removeLink(chatId, request);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Link removed successfully, invalidating Redis cache for chat {}", chatId);
            redisTemplate.delete(chatId);
        }
        return response;
    }
}
