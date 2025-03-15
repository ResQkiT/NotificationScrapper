package backend.academy.scrapper.processor;

import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.dto.GitHubResponseDto;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GitHubProcessor extends Processor {

    private final GitHubClient client;

    @Autowired
    public GitHubProcessor(GitHubClient client, LinkService linkService) {
        super("github.com", linkService);
        this.client = client;
    }

    @Override
    public String process(Link link) {
        GitHubResponseDto linkInfo = fetchRepositoryInfo(link);

        if (isFirstTimeProcessing(link)) {
            System.out.println("First time processing " + link);
            updateLink(link, linkInfo.updatedAt());
            return null;
        }

        if (hasUpdates(linkInfo.updatedAt(), link)) {
            updateLink(link, linkInfo.updatedAt());
            return "Есть изменения";
        }

        return null;
    }

    private GitHubResponseDto fetchRepositoryInfo(Link link) {
        URI uri = URI.create(link.url());
        String ownerAndRepo = uri.getPath().substring(1);

        var response = client.getRepositoryInfo(ownerAndRepo);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("GitHub API is unavailable");
        }

        return response.getBody();
    }
}
