package backend.academy.scrapper.processor;

import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.dto.GitHubResponseDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.service.LinkService;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.time.OffsetDateTime;

@Component
public class GitHubProcessor extends Processor{

    private final GitHubClient client;
    private final LinkService linkService;

    public GitHubProcessor(GitHubClient client, LinkService linkService) {
        super("github.com", linkService);
        this.client = client;
        this.linkService = linkService;
    }

    @Override
    public String process(Link link) {
        URI uri = URI.create(link.url());

        String ownerAndRepo = uri.getPath().substring(1);

        var response = client.getRepositoryInfo(ownerAndRepo);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return "Ошибка при запросе к GitHub API";
        }

        GitHubResponseDto linkInfo = response.getBody();
        System.out.println("linkInfo: " + linkInfo);
        if (hasUpdates(linkInfo, link)) {
            System.out.println("новоя точкаЖ "+ linkInfo.updatedAt());
            link.lastUpdatedAt(linkInfo.updatedAt());
            linkService.updateLink(link);
            return "Есть изменения";
        }

        return null;
    }

    private boolean hasUpdates(GitHubResponseDto resp, Link link) {
        //TODO тут кринжанул
        System.out.println(link.lastUpdatedAt());
        return link.lastUpdatedAt() == null || resp.updatedAt().isAfter(OffsetDateTime.from(link.lastUpdatedAt()));
    }
}
