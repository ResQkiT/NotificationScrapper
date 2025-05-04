package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.clients.api.GitHubApiClient;
import backend.academy.scrapper.config.DomainsConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.git.GitHubIssueDto;
import backend.academy.scrapper.dto.git.GitHubPullRequestDto;
import backend.academy.scrapper.dto.git.GitHubResponseDto;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@WireMockTest(httpPort = 8089)
class GitHubApiClientTest {

    private GitHubApiClient gitHubClient;

    @BeforeEach
    void setUp() {
        ScrapperConfig scrapperConfig = mock(ScrapperConfig.class);
        when(scrapperConfig.githubToken()).thenReturn("test-token");

        DomainsConfig domainsConfig = new DomainsConfig("http://localhost:8089", "", "", "");

        RestClient.Builder restClientBuilder = RestClient.builder();
        gitHubClient = new GitHubApiClient(restClientBuilder, scrapperConfig, domainsConfig);
    }

    @Test
    @DisplayName("Получение информации о репозитории: при успешном запросе возвращаются данные репозитория")
    void testGetRepositoryInfo_whenOk_thenReturnRepositoryData() {
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

    @Test
    @DisplayName("Получение списка issues репозитория: при успешном запросе возвращаются данные issues")
    void testGetRepositoryIssues_whenOk_thenReturnIssuesData() {
        String ownerAndRepo = "testOwner/testRepo";
        stubFor(get("/repos/" + ownerAndRepo + "/issues")
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{" + "\"id\": 1," + "\"title\": \"Test Issue\"," + "\"state\": \"open\"" + "}]")));

        ResponseEntity<List<GitHubIssueDto>> response = gitHubClient.getRepositoryIssues(ownerAndRepo);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().id()).isEqualTo(1);
        assertThat(response.getBody().getFirst().title()).isEqualTo("Test Issue");
    }

    @Test
    @DisplayName("Получение списка pull requests репозитория: при успешном запросе возвращаются данные pull requests")
    void testGetRepositoryPullRequests_whenOk_thenReturnPullRequestsData() {
        String ownerAndRepo = "testOwner/testRepo";
        stubFor(get("/repos/" + ownerAndRepo + "/pulls")
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{" + "\"id\": 1,"
                                + "\"title\": \"Test Pull Request\","
                                + "\"state\": \"open\""
                                + "}]")));

        ResponseEntity<List<GitHubPullRequestDto>> response = gitHubClient.getRepositoryPullRequests(ownerAndRepo);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().id()).isEqualTo(1);
        assertThat(response.getBody().getFirst().title()).isEqualTo("Test Pull Request");
    }
}
