package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.dto.GitHubResponseDto;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@WireMockTest(httpPort = 8089)
class GitHubClientTest {

    private GitHubClient gitHubClient;

    @BeforeEach
    void setUp() {
        ScrapperConfig scrapperConfig = mock(ScrapperConfig.class);
        when(scrapperConfig.githubToken()).thenReturn("test-token");

        DomainsConfig domainsConfig = new DomainsConfig("http://localhost:8089", "", "", "");

        RestClient.Builder restClientBuilder = RestClient.builder();
        gitHubClient = new GitHubClient(restClientBuilder, scrapperConfig, domainsConfig);
    }

    @Test
    void testGetRepositoryInfo_whenOk_thenReturnRepositoryData() {
        // Arrange
        String ownerAndRepo = "testOwner/testRepo";
        stubFor(get("/repos/" + ownerAndRepo)
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{" + "\"full_name\": \"testOwner/testRepo\","
                                + "\"updated_at\": \"2024-01-01T12:00:00Z\","
                                + "\"default_branch\": \"main\""
                                + "}")));

        ResponseEntity<GitHubResponseDto> response = gitHubClient.getRepositoryInfo(ownerAndRepo);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().fullName()).isEqualTo("testOwner/testRepo");
        assertThat(response.getBody().updatedAt()).isEqualTo(OffsetDateTime.parse("2024-01-01T12:00:00Z"));
        assertThat(response.getBody().defaultBranch()).isEqualTo("main");
    }
}
