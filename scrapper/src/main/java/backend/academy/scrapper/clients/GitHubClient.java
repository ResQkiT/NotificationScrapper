package backend.academy.scrapper.clients;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.git.GitHubIssueDto;
import backend.academy.scrapper.dto.git.GitHubPullRequestDto;
import backend.academy.scrapper.dto.git.GitHubResponseDto;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubClient extends Client {
    private final String token;

    public GitHubClient(
            RestClient.Builder restClientBuilder, ScrapperConfig scrapperConfig, DomainsConfig domainsConfig) {
        super(restClientBuilder.baseUrl(domainsConfig.github()).build());
        this.token = scrapperConfig.githubToken();
    }

    public ResponseEntity<GitHubResponseDto> getRepositoryInfo(String ownerAndRepo) {
        return client().get()
                .uri("/repos/" + ownerAndRepo)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(GitHubResponseDto.class);
    }

    public ResponseEntity<List<GitHubIssueDto>> getRepositoryIssues(String ownerAndRepo) {
        return client().get()
                .uri("/repos/" + ownerAndRepo + "/issues")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
    }

    public ResponseEntity<List<GitHubPullRequestDto>> getRepositoryPullRequests(String ownerAndRepo) {
        return client().get()
                .uri("/repos/" + ownerAndRepo + "/pulls")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
    }
}
