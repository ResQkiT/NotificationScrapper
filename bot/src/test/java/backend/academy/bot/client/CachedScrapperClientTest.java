package backend.academy.bot.client;

import backend.academy.bot.clients.CachedScrapperClient;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.config.DomainsConfig;
import backend.academy.bot.config.RedisConfig;
import backend.academy.bot.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(RedisConfig.class)
public class CachedScrapperClientTest {

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.0.12-alpine")
        .withExposedPorts(6379);

    @Autowired
    private CachedScrapperClient cachedScrapperClient;

    @Autowired
    private RedisTemplate<Long, LinkResponse> redisTemplate;

    @Autowired
    private RestClient.Builder restClientBuilder;

    private MockRestServiceServer mockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
        registry.add("domains.scrapper", () -> "http://localhost:8080");
    }

    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("Кэширование: данные из кэша при наличии")
    void getTrackedLinks_ShouldReturnCachedData() {
        // Arrange
        Long chatId = 1L;
        LinkResponse cachedLink = new LinkResponse(1L, "https://cached.com", List.of(), List.of());
        redisTemplate.opsForSet().add(chatId, cachedLink);

        // Act
        ResponseEntity<ListLinksResponse> result = cachedScrapperClient.getTrackedLinks(chatId);

        // Assert
        assertThat(result.getBody().links()).containsExactly(cachedLink);
        mockServer.verify();
    }

    @Test
    @DisplayName("Кэширование: сохранение данных при первом запросе")
    void getTrackedLinks_ShouldCacheDataFromParent() {
        // Arrange
        Long chatId = 2L;
        String expectedUrl = "http://localhost:8080/links";
        LinkResponse serverLink = new LinkResponse(2L, "https://server.com", List.of(), List.of());

        mockServer.expect(requestTo(expectedUrl))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Tg-Chat-Id", chatId.toString()))
            .andRespond(withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "links": [
                            {
                                "id": 2,
                                "url": "https://server.com",
                                "tags": [],
                                "filters": []
                            }
                        ],
                        "size": 1
                    }
                    """));

        // Act
        ResponseEntity<ListLinksResponse> response = cachedScrapperClient.getTrackedLinks(chatId);

        // Assert
        Set<LinkResponse> cached = redisTemplate.opsForSet().members(chatId);
        assertThat(cached).containsExactly(serverLink);
        mockServer.verify();
    }

    @Test
    @DisplayName("Добавление ссылки: инвалидация кэша")
    void addLink_ShouldInvalidateCache() {
        // Arrange
        Long chatId = 3L;
        String expectedUrl = "http://localhost:8080/links";
        AddLinkRequest request = new AddLinkRequest("https://new.com", List.of(), List.of());

        redisTemplate.opsForSet().add(chatId, new LinkResponse(3L, "https://old.com", List.of(), List.of()));

        mockServer.expect(requestTo(expectedUrl))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Tg-Chat-Id", chatId.toString()))
            .andRespond(withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                        "id": 4,
                        "url": "https://new.com",
                        "tags": [],
                        "filters": []
                    }
                    """));

        // Act
        cachedScrapperClient.addLink(chatId, request);

        // Assert
        assertThat(redisTemplate.hasKey(chatId)).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("Удаление ссылки: сохранение кэша при ошибке")
    void removeLink_ShouldKeepCacheOnFailure() {
        // Arrange
        Long chatId = 4L;
        String expectedUrl = "http://localhost:8080/links";
        RemoveLinkRequest request = new RemoveLinkRequest("https://remove.com");
        LinkResponse cachedLink = new LinkResponse(5L, "https://keep.com", List.of(), List.of());

        redisTemplate.opsForSet().add(chatId, cachedLink);

        mockServer.expect(requestTo(expectedUrl))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header("Tg-Chat-Id", chatId.toString()))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act
        ResponseEntity<LinkResponse> response = cachedScrapperClient.removeLink(chatId, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(redisTemplate.opsForSet().members(chatId)).contains(cachedLink);
        mockServer.verify();
    }
}
