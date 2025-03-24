package backend.academy.scrapper.processor;

import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.dto.git.GitHubIssueDto;
import backend.academy.scrapper.dto.git.GitHubPullRequestDto;
import backend.academy.scrapper.dto.git.GitHubResponseDto;
import backend.academy.scrapper.model.GitHubLink;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.GitHubLinkService;
import backend.academy.scrapper.service.ILinkService;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class GitHubProcessor extends Processor {

    private final GitHubClient client;
    private final GitHubLinkService gitHubLinkService;

    @Autowired
    public GitHubProcessor(GitHubClient client, ILinkService linkService, GitHubLinkService gitHubLinkService) {
        super("github.com", linkService);
        this.client = client;
        this.gitHubLinkService = gitHubLinkService;
    }

    @Override
    public String process(Link link) {
        GitHubResponseDto linkInfo = fetchRepositoryInfo(link);
        GitHubIssueDto currentIssue = fetchIssue(link).getFirst();
        GitHubPullRequestDto currentPull = fetchPullRequest(link).getFirst();

        GitHubLink relevantLink = new GitHubLink(
                currentIssue.id(),
                currentIssue.title(),
                currentIssue.user().username(),
                currentIssue.createdAt(),
                currentIssue.body(),
                currentPull.id(),
                currentPull.title(),
                currentPull.user().username(),
                currentPull.createdAt(),
                currentPull.body());
        relevantLink.id(link.id());

        if (isFirstTimeProcessing(link)) {
            System.out.println("First time processing " + link);
            touchLink(link);
            updateOrSave(relevantLink);
            return null;
        }
        touchLink(link);
        GitHubLink existedLink = gitHubLinkService.findById(link.id()).orElse(null);
        StringBuilder answer = new StringBuilder();

        //Todo: пока работаем только с issues (C точки зрения гита pr = issues, так что в теории этого достаточно)
        if (!Objects.equals(existedLink.lastIssueId(), currentIssue.id())) {
            updateOrSave(relevantLink);
            answer.append("Появилось новое issue: " + currentIssue.title() + "\n");
            answer.append("Автор:" + currentIssue.user().username() + "\n");
            answer.append("Дата создания:" + currentIssue.createdAt() + "\n");
            answer.append("Описание:" + cutBody(currentIssue.body()) + "\n");

            return answer.toString();
        }

        return null;
    }

    private void updateOrSave(GitHubLink link) {
        if (gitHubLinkService.findById(link.id()).isPresent()) {
            gitHubLinkService.updateLink(link);
        } else {
            gitHubLinkService.createLink(link);
        }
    }

    private GitHubResponseDto fetchRepositoryInfo(Link link) {
        String ownerAndRepo = getOwnerAndRepo(link);

        var response = client.getRepositoryInfo(ownerAndRepo);

        assertSuccess(response, new RuntimeException("Cant fetch repository info: GitHub API is unavailable"));
        return response.getBody();
    }

    private List<GitHubIssueDto> fetchIssue(Link link) {
        String ownerAndRepo = getOwnerAndRepo(link);

        var response = client.getRepositoryIssues(ownerAndRepo);

        assertSuccess(response, new RuntimeException("Cant fetch repository issues: GitHub API is unavailable"));
        return response.getBody();
    }

    private List<GitHubPullRequestDto> fetchPullRequest(Link link) {
        String ownerAndRepo = getOwnerAndRepo(link);

        var response = client.getRepositoryPullRequests(ownerAndRepo);

        assertSuccess(response, new RuntimeException("Cant fetch repository pulls: GitHub API is unavailable"));
        return response.getBody();
    }

    private String getOwnerAndRepo(Link link) {
        URI uri = URI.create(link.url());
        String ownerAndRepo = uri.getPath().substring(1);
        return ownerAndRepo;
    }
}
