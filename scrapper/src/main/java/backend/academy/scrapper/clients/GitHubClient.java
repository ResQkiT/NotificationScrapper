package backend.academy.scrapper.clients;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.GitHubResponseDto;
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
}
