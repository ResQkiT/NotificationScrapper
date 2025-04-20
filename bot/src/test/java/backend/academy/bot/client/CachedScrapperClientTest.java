package backend.academy.bot.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import backend.academy.bot.clients.CachedScrapperClient;
import backend.academy.bot.config.DomainsConfig;
import backend.academy.bot.config.RedisConfig;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = {"spring.caching=true", "spring.data.redis.host=localhost", "spring.data.redis.port=6379"})
@Testcontainers
@Import({CachedScrapperClientTest.TestConfig.class, RedisConfig.class})
public class CachedScrapperClientTest {

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(
                    DockerImageName.parse("redis:7.0.12-alpine"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));

    @Autowired
    @Qualifier("linkResponseRedisTemplate")
    private RedisTemplate<Long, LinkResponse> redisTemplate;

    @SpyBean
    private CachedScrapperClient cachedScrapperClient;

    @Autowired
    private RestClient restClient;

    @Configuration
    static class TestConfig {
        @Value("${spring.data.redis.host}")
        private String redisHost;

        @Value("${spring.data.redis.port}")
        private int redisPort;

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            System.out.println("DEBUG: Creating explicit RedisConnectionFactory for " + redisHost + ":" + redisPort);
            RedisStandaloneConfiguration standaloneConfiguration =
                    new RedisStandaloneConfiguration(redisHost, redisPort);
            LettuceClientConfiguration clientConfig =
                    LettuceClientConfiguration.builder().build();
            return new LettuceConnectionFactory(standaloneConfiguration, clientConfig);
        }

        @Bean
        public RestClient mockRestClient() {
            return Mockito.mock(RestClient.class);
        }

        @Bean
        public RestClient.Builder restClientBuilder(RestClient mockRestClient) {
            RestClient.Builder mockBuilder = Mockito.mock(RestClient.Builder.class);
            when(mockBuilder.baseUrl(anyString())).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockRestClient);
            return mockBuilder;
        }

        @Bean
        public DomainsConfig domainsConfig() {
            return new DomainsConfig("http://mocked-scrapper-url");
        }
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    }

    @BeforeEach
    void cleanUp() {
        assertThat(redisTemplate).as("RedisTemplate должен быть внедрен").isNotNull();
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            System.err.println("WARNING: Failed to flush Redis cache in BeforeEach: " + e.getMessage());
        }
        assertThat(cachedScrapperClient)
                .as("CachedScrapperClient должен быть внедрен")
                .isNotNull();
        Mockito.reset(restClient);
    }

    @Test
    @DisplayName(
            "getTrackedLinks - Промах кэша: Вызывает ScrapperClient (через RestClient), получает данные и кэширует")
    void getTrackedLinks_cacheMiss_callsSuperAndCaches() {
        Long chatId = 123L;
        LinkResponse link1 = new LinkResponse(1L, "http://example.com/link1", List.of("tag1"), List.of("filterA"));
        LinkResponse link2 = new LinkResponse(2L, "http://example.com/link2", List.of("tag2", "tag3"), List.of());
        ListLinksResponse scrapperApiResponse = new ListLinksResponse(List.of(link1, link2), 2);
        ResponseEntity<ListLinksResponse> expectedResponseFromScrapper =
                new ResponseEntity<>(scrapperApiResponse, HttpStatus.OK);

        assertThat(redisTemplate.hasKey(chatId)).isFalse();

        RestClient.RequestHeadersUriSpec requestHeadersUriSpecMock = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpecMock = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpecMock = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(anyString(), eq(chatId))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toEntity(ListLinksResponse.class)).thenReturn(expectedResponseFromScrapper);

        ResponseEntity<ListLinksResponse> actualResponse = cachedScrapperClient.getTrackedLinks(chatId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(scrapperApiResponse);
        assertThat(actualResponse.getBody().links()).containsExactlyInAnyOrder(link1, link2);

        verify(restClient, times(1)).get();
        verify(responseSpecMock, times(1)).toEntity(ListLinksResponse.class);

        assertThat(redisTemplate.hasKey(chatId)).isTrue();
        Set<LinkResponse> cachedLinks = redisTemplate.opsForSet().members(chatId);
        assertThat(cachedLinks).containsExactlyInAnyOrder(link1, link2);
    }

    @Test
    @DisplayName("getTrackedLinks - Попадание в кэш: Возвращает из кэша и НЕ вызывает ScrapperClient (RestClient)")
    void getTrackedLinks_cacheHit_returnsFromCacheAndDoesNotCallSuper() {
        Long chatId = 456L;
        LinkResponse cachedLink1 = new LinkResponse(10L, "http://example.com/cached1", List.of("cacheTag"), List.of());
        LinkResponse cachedLink2 =
                new LinkResponse(11L, "http://example.com/cached2", List.of(), List.of("cacheFilter"));

        redisTemplate.opsForSet().add(chatId, cachedLink1, cachedLink2);
        assertThat(redisTemplate.hasKey(chatId)).isTrue();

        ResponseEntity<ListLinksResponse> actualResponse = cachedScrapperClient.getTrackedLinks(chatId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody().links()).containsExactlyInAnyOrder(cachedLink1, cachedLink2);
        assertThat(actualResponse.getBody().size()).isEqualTo(2);

        verify(restClient, never()).get();
        verify(restClient, never()).post();
        verify(restClient, never()).delete();

        assertThat(redisTemplate.hasKey(chatId)).isTrue();
    }

    @Test
    @DisplayName("getTrackedLinks - Промах кэша: ScrapperClient возвращает пустой список, Кэш НЕ создается")
    void getTrackedLinks_cacheMiss_superReturnsEmptyList_cacheIsEmpty() {
        Long chatId = 789L;
        ListLinksResponse emptyScrapperApiResponse = new ListLinksResponse(List.of(), 0);
        ResponseEntity<ListLinksResponse> emptyResponseFromScrapper =
                new ResponseEntity<>(emptyScrapperApiResponse, HttpStatus.OK);

        assertThat(redisTemplate.hasKey(chatId)).isFalse();

        RestClient.RequestHeadersUriSpec requestHeadersUriSpecMock = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpecMock = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpecMock = mock(RestClient.ResponseSpec.class);
        when(restClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(anyString(), eq(chatId))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toEntity(ListLinksResponse.class)).thenReturn(emptyResponseFromScrapper);

        ResponseEntity<ListLinksResponse> actualResponse = cachedScrapperClient.getTrackedLinks(chatId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(emptyScrapperApiResponse);
        assertThat(actualResponse.getBody().links()).isEmpty();

        verify(restClient, times(1)).get();
        verify(responseSpecMock, times(1)).toEntity(ListLinksResponse.class);

        assertThat(redisTemplate.hasKey(chatId)).isFalse();
    }

    @Test
    @DisplayName("addLink - Успех: Вызывает ScrapperClient (RestClient) и инвалидирует кэш")
    void addLink_success_callsSuperAndInvalidatesCache() {
        Long chatId = 111L;
        AddLinkRequest request =
                new AddLinkRequest("http://example.com/newlink", List.of("tagA", "tagB"), List.of("filterX"));
        LinkResponse scrapperApiResponse = new LinkResponse(50L, request.link(), request.tags(), request.filters());
        ResponseEntity<LinkResponse> successResponseFromScrapper =
                new ResponseEntity<>(scrapperApiResponse, HttpStatus.OK);

        redisTemplate
                .opsForSet()
                .add(chatId, new LinkResponse(100L, "http://example.com/oldlink", List.of(), List.of()));
        assertThat(redisTemplate.hasKey(chatId)).isTrue();

        RestClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpecMock = mock(RestClient.RequestBodySpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpecMock = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpecMock = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString(), eq(chatId))).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.body(eq(request))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toEntity(LinkResponse.class)).thenReturn(successResponseFromScrapper);

        ResponseEntity<LinkResponse> actualResponse = cachedScrapperClient.addLink(chatId, request);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(scrapperApiResponse);
        verify(restClient, times(1)).post();
        verify(responseSpecMock, times(1)).toEntity(LinkResponse.class);
        assertThat(redisTemplate.hasKey(chatId)).isFalse();
    }

    @Test
    @DisplayName("addLink - Ошибка: Вызывает ScrapperClient (RestClient), но Не инвалидирует кэш")
    void addLink_failure_callsSuperAndDoesNotInvalidateCache() {
        Long chatId = 222L;
        AddLinkRequest request = new AddLinkRequest("http://example.com/failinglink", List.of(), List.of());
        ResponseEntity<LinkResponse> errorResponseFromScrapper = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        LinkResponse cachedLink = new LinkResponse(200L, "http://example.com/oldlink", List.of("cached"), List.of());
        redisTemplate.opsForSet().add(chatId, cachedLink);
        assertThat(redisTemplate.hasKey(chatId)).isTrue();

        RestClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpecMock = mock(RestClient.RequestBodySpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpecMock = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpecMock = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString(), eq(chatId))).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.body(eq(request))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toEntity(LinkResponse.class)).thenReturn(errorResponseFromScrapper);

        ResponseEntity<LinkResponse> actualResponse = cachedScrapperClient.addLink(chatId, request);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(restClient, times(1)).post();
        verify(responseSpecMock, times(1)).toEntity(LinkResponse.class);

        assertThat(redisTemplate.hasKey(chatId)).isTrue();
        assertThat(redisTemplate.opsForSet().members(chatId)).containsExactly(cachedLink);
    }

    @Test
    @DisplayName("removeLink - Успех: Вызывает ScrapperClient (RestClient) и инвалидирует кэш")
    void removeLink_success_callsSuperAndInvalidatesCache() {
        Long chatId = 333L;
        RemoveLinkRequest request = new RemoveLinkRequest("http://example.com/linktoremove");
        LinkResponse scrapperApiResponse = new LinkResponse(30L, request.link(), List.of(), List.of("removed"));
        ResponseEntity<LinkResponse> successResponseFromScrapper =
                new ResponseEntity<>(scrapperApiResponse, HttpStatus.OK);

        redisTemplate
                .opsForSet()
                .add(
                        chatId,
                        new LinkResponse(31L, request.link(), List.of(), List.of()),
                        new LinkResponse(32L, "http://example.com/other", List.of("other"), List.of()));
        assertThat(redisTemplate.hasKey(chatId)).isTrue();

        RestClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpecMock = mock(RestClient.RequestBodySpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpecMock = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpecMock = mock(RestClient.ResponseSpec.class);

        when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString(), eq(chatId))).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.body(eq(request))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toEntity(LinkResponse.class)).thenReturn(successResponseFromScrapper);

        ResponseEntity<LinkResponse> actualResponse = cachedScrapperClient.removeLink(chatId, request);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(scrapperApiResponse);

        verify(restClient, times(1)).method(HttpMethod.DELETE);
        verify(responseSpecMock, times(1)).toEntity(LinkResponse.class);

        assertThat(redisTemplate.hasKey(chatId)).isFalse();
    }

    @Test
    @DisplayName("removeLink - Ошибка: Вызывает ScrapperClient (RestClient), но Не инвалидирует кэш")
    void removeLink_failure_callsSuperAndDoesNotInvalidateCache() {
        Long chatId = 444L;
        RemoveLinkRequest request = new RemoveLinkRequest("http://example.com/nonexistentlink");
        ResponseEntity<LinkResponse> errorResponseFromScrapper = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        LinkResponse cachedLink = new LinkResponse(40L, "http://example.com/existing", List.of("cached"), List.of());
        redisTemplate.opsForSet().add(chatId, cachedLink);
        assertThat(redisTemplate.hasKey(chatId)).isTrue();

        RestClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpecMock = mock(RestClient.RequestBodySpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpecMock = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpecMock = mock(RestClient.ResponseSpec.class);

        when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString(), eq(chatId))).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.body(eq(request))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toEntity(LinkResponse.class)).thenReturn(errorResponseFromScrapper);

        ResponseEntity<LinkResponse> actualResponse = cachedScrapperClient.removeLink(chatId, request);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(restClient, times(1)).method(HttpMethod.DELETE);
        verify(responseSpecMock, times(1)).toEntity(LinkResponse.class);

        assertThat(redisTemplate.hasKey(chatId)).isTrue();
        assertThat(redisTemplate.opsForSet().members(chatId)).containsExactly(cachedLink);
    }
}
