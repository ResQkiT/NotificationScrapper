package backend.academy.bot.client;

import backend.academy.bot.clients.CachedScrapperClient; // Убедись, что пакет правильный
import backend.academy.bot.clients.ScrapperClient; // Импортируем, если нужен для каста или проверки
import backend.academy.bot.config.DomainsConfig;
import backend.academy.bot.config.RedisConfig; // Предполагаем, что этот конфиг корректен
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // ИСПОЛЬЗУЕМ MockBean для RestClient.Builder
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
    classes = {
        CachedScrapperClient.class, // Явно подключаем тестируемый компонент
        RedisConfig.class,
        CachedScrapperClientTest.TestConfig.class
    },
    properties = {
        "spring.caching=true",
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@ActiveProfiles("test")
@Testcontainers
public class CachedScrapperClientTest {

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.0.12-alpine")
        .withExposedPorts(6379);

    @Configuration
    @EnableRedisRepositories
    static class TestConfig {

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(
                    REDIS.getHost(),
                    REDIS.getFirstMappedPort()
                )
            );
        }

        @Bean
        @Primary
        public RedisTemplate<Long, LinkResponse> linkResponseRedisTemplate(
            RedisConnectionFactory connectionFactory
        ) {
            RedisTemplate<Long, LinkResponse> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new GenericToStringSerializer<>(Long.class));
            template.setValueSerializer(new Jackson2JsonRedisSerializer<>(LinkResponse.class));
            return template;
        }

        @Bean
        @Primary
        public RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        @Primary
        public DomainsConfig domainsConfig() {
            return new DomainsConfig("http://dummy-url");
        }
    }

    @Autowired
    private CachedScrapperClient cachedScrapperClient;

    @MockBean
    private ScrapperClient scrapperClient;

    @Autowired
    private RedisTemplate<Long, LinkResponse> redisTemplate;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    }


    @Test
    @DisplayName("getTrackedLinks - Кэш попадание")
    void getTrackedLinks_cacheHit_returnsFromCache() {
        Long chatId = 456L;
        LinkResponse cachedLink1 = new LinkResponse(1L, "https://cached1.com",
            List.of("news", "tech"), List.of("filter1"));
        LinkResponse cachedLink2 = new LinkResponse(2L, "https://cached2.com",
            List.of("blog"), List.of());

        // Заполняем кэш
        redisTemplate.opsForSet().add(chatId, cachedLink1, cachedLink2);

        ResponseEntity<ListLinksResponse> result = cachedScrapperClient.getTrackedLinks(chatId);

        assertThat(result.getBody().links())
            .containsExactlyInAnyOrder(cachedLink1, cachedLink2);
        verify(scrapperClient, never()).getTrackedLinks(any());
    }

    @Test
    @DisplayName("addLink - Успешное добавление с инвалидацией кэша")
    void addLink_successfulAddition_invalidatesCache() {
        Long chatId = 123L;
        AddLinkRequest request = new AddLinkRequest(
            "https://new-link.com",
            List.of("new", "article"),
            List.of("strict-filter")
        );
        LinkResponse response = new LinkResponse(3L, request.link(),
            request.tags(), request.filters());

        // Предварительное заполнение кэша
        redisTemplate.opsForSet().add(chatId,
            new LinkResponse(4L, "https://old-link.com", List.of(), List.of()));

        when(scrapperClient.addLink(eq(chatId), eq(request)))
            .thenReturn(ResponseEntity.ok(response));

        ResponseEntity<LinkResponse> result = cachedScrapperClient.addLink(chatId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        assertThat(redisTemplate.hasKey(chatId)).isFalse();
    }

    @Test
    @DisplayName("addLink - Ошибка добавления (кэш не инвалидируется)")
    void addLink_failedAddition_keepsCache() {
        Long chatId = 123L;
        AddLinkRequest request = new AddLinkRequest(
            "https://invalid-link.com",
            List.of(),
            List.of()
        );
        LinkResponse cachedLink = new LinkResponse(5L, "https://valid-link.com",
            List.of("important"), List.of("filter"));

        // Заполняем кэш
        redisTemplate.opsForSet().add(chatId, cachedLink);

        when(scrapperClient.addLink(eq(chatId), eq(request)))
            .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

        ResponseEntity<LinkResponse> result = cachedScrapperClient.addLink(chatId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(redisTemplate.hasKey(chatId)).isTrue();
        assertThat(redisTemplate.opsForSet().members(chatId)).contains(cachedLink);
    }

    @Test
    @DisplayName("removeLink - Успешное удаление с инвалидацией кэша")
    void removeLink_successfulRemoval_invalidatesCache() {
        Long chatId = 789L;
        RemoveLinkRequest request = new RemoveLinkRequest("https://remove-me.com");
        LinkResponse response = new LinkResponse(6L, request.link(),
            List.of(), List.of());

        // Заполняем кэш
        redisTemplate.opsForSet().add(chatId,
            new LinkResponse(6L, request.link(), List.of(), List.of()),
            new LinkResponse(7L, "https://keep-me.com", List.of(), List.of()));

        when(scrapperClient.removeLink(eq(chatId), eq(request)))
            .thenReturn(ResponseEntity.ok(response));

        ResponseEntity<LinkResponse> result = cachedScrapperClient.removeLink(chatId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(redisTemplate.hasKey(chatId)).isFalse();
    }

    @Test
    @DisplayName("removeLink - Ошибка удаления (кэш остается)")
    void removeLink_failedRemoval_keepsCache() {
        Long chatId = 123L;
        RemoveLinkRequest request = new RemoveLinkRequest("https://non-existent.com");
        LinkResponse cachedLink = new LinkResponse(8L, "https://existing.com",
            List.of("keep"), List.of());

        // Заполняем кэш
        redisTemplate.opsForSet().add(chatId, cachedLink);

        when(scrapperClient.removeLink(eq(chatId), eq(request)))
            .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        ResponseEntity<LinkResponse> result = cachedScrapperClient.removeLink(chatId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(redisTemplate.hasKey(chatId)).isTrue();
        assertThat(redisTemplate.opsForSet().members(chatId)).contains(cachedLink);
    }

    @Test
    @DisplayName("deleteChat - Инвалидация кэша")
    void deleteChat_invalidatesCache() {
        Long chatId = 999L;
        LinkResponse cachedLink = new LinkResponse(9L, "https://to-delete.com",
            List.of(), List.of());

        // Заполняем кэш
        redisTemplate.opsForSet().add(chatId, cachedLink);

        when(scrapperClient.deleteChat(eq(chatId)))
            .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Void> result = cachedScrapperClient.deleteChat(chatId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(redisTemplate.hasKey(chatId)).isFalse();
    }

    @Test
    @DisplayName("registerChat - Не влияет на кэш")
    void registerChat_doesNotAffectCache() {
        Long chatId = 123L;
        LinkResponse cachedLink = new LinkResponse(10L, "https://existing.com",
            List.of(), List.of());

        // Заполняем кэш
        redisTemplate.opsForSet().add(chatId, cachedLink);

        when(scrapperClient.registerChat(eq(chatId)))
            .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Void> result = cachedScrapperClient.registerChat(chatId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(redisTemplate.hasKey(chatId)).isTrue();
        assertThat(redisTemplate.opsForSet().members(chatId)).contains(cachedLink);
    }
}
