package backend.academy.bot.clients;

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

@Component
@Slf4j
@Primary
@ConditionalOnProperty(name = "spring.caching", havingValue = "true")
public class CachedScrapperClient implements IClient {

    private final RedisTemplate<Long, LinkResponse> redisTemplate;
    private final ScrapperClientSender scrapperClientSender;

    @Autowired
    public CachedScrapperClient(
            RedisTemplate<Long, LinkResponse> redisTemplate, ScrapperClientSender scrapperClientSender) {
        this.scrapperClientSender = scrapperClientSender;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ResponseEntity<Void> registerChat(Long id) {
        return scrapperClientSender.registerChat(id);
    }

    @Override
    public ResponseEntity<Void> deleteChat(Long id) {
        return scrapperClientSender.deleteChat(id);
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
        ResponseEntity<ListLinksResponse> response = scrapperClientSender.getTrackedLinks(chatId);
        if (response.getBody().size() > 0) {
            redisTemplate.opsForSet().add(chatId, response.getBody().links().toArray(new LinkResponse[0]));
        } else {
            redisTemplate.delete(chatId);
        }
        return response;
    }

    @Override
    public ResponseEntity<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        ResponseEntity<LinkResponse> response = scrapperClientSender.addLink(chatId, request);
        log.info("Add Link");
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Link added successfully, invalidating Redis cache for chat {}", chatId);
            redisTemplate.delete(chatId);
        }
        return response;
    }

    @Override
    public ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request) {
        ResponseEntity<LinkResponse> response = scrapperClientSender.removeLink(chatId, request);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Link removed successfully, invalidating Redis cache for chat {}", chatId);
            redisTemplate.delete(chatId);
        }
        return response;
    }
}
