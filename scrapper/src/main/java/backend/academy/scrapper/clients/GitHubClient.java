package backend.academy.scrapper.clients;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.GitHubResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubClient extends Client{

    private static final String BASE_GITHUB_URL = "https://api.github.com";
    private final String token;

    public GitHubClient(RestClient.Builder restClientBuilder, ScrapperConfig scrapperConfig) {
        super(restClientBuilder
            .baseUrl(BASE_GITHUB_URL)
            .build());

        this.token = scrapperConfig.githubToken();
    }

    public ResponseEntity<GitHubResponseDto> getRepositoryInfo(String ownerAndRepo){
        System.out.println("/repos/" + ownerAndRepo);
        System.out.println(token);
        return client().get()
            .uri("/repos/" + ownerAndRepo)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .toEntity(GitHubResponseDto.class);
    }
}
