package backend.academy.scrapper.processor;

import backend.academy.scrapper.clients.api.GitHubApiClient;
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
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GitHubProcessor extends Processor {

    private final GitHubApiClient client;
    private final GitHubLinkService gitHubLinkService;

    @Autowired
    public GitHubProcessor(GitHubApiClient client, ILinkService linkService, GitHubLinkService gitHubLinkService) {
        super("github.com", linkService);
        this.client = client;
        this.gitHubLinkService = gitHubLinkService;
    }

    @Override
    public String process(Link link) {
        Optional<GitHubIssueDto> lastIssue = getLastIssue(link);
        Optional<GitHubPullRequestDto> lastPull = getLastPullRequest(link);

        if (lastIssue.isEmpty() && lastPull.isEmpty()) {
            return null;
        }

        GitHubLink relevantLink = buildRelevantLink(link, lastIssue, lastPull);
        return handleUpdates(link, relevantLink, lastIssue, lastPull);
    }

    private String handleUpdates(
            Link link,
            GitHubLink newLink,
            Optional<GitHubIssueDto> lastIssue,
            Optional<GitHubPullRequestDto> lastPull) {
        StringBuilder response = new StringBuilder();
        boolean hasUpdates = false;

        if (isFirstTimeProcessing(link)) {
            updateOrSave(newLink);
            touchLink(link);
            return null;
        }

        GitHubLink existingLink = gitHubLinkService.findById(link.id()).orElse(null);
        if (existingLink == null) {
            return null;
        }

        // Check issues
        if (lastIssue.isPresent()) {
            GitHubIssueDto issue = lastIssue.get();
            if (!Objects.equals(existingLink.lastIssueId(), issue.id())) {
                response.append(formatIssueMessage(issue));
                hasUpdates = true;
            }
        }

        // Check pull requests
        if (lastPull.isPresent()) {
            GitHubPullRequestDto pull = lastPull.get();
            if (!Objects.equals(existingLink.lastPullRequestId(), pull.id())) {
                response.append(formatPullRequestMessage(pull));
                hasUpdates = true;
            }
        }

        if (hasUpdates) {
            updateOrSave(newLink);
            touchLink(link);
            return response.toString();
        }

        touchLink(link);
        return null;
    }

    private GitHubLink buildRelevantLink(
            Link link, Optional<GitHubIssueDto> issue, Optional<GitHubPullRequestDto> pull) {
        GitHubLink gitHubLink = new GitHubLink();
        gitHubLink.id(link.id());

        issue.ifPresent(i -> {
            gitHubLink
                    .lastIssueId(i.id())
                    .lastIssueTitle(i.title())
                    .issueCreatorUsername(i.user() != null ? i.user().username() : "Unknown")
                    .issueCreatedAt(i.createdAt())
                    .issuePreviewDescription(cutBody(i.body()));
        });

        pull.ifPresent(p -> {
            gitHubLink
                    .lastPullRequestId(p.id())
                    .lastPullRequestTitle(p.title())
                    .pullCreatorUsername(p.user() != null ? p.user().username() : "Unknown")
                    .pullCreatedAt(p.createdAt())
                    .pullPreviewDescription(cutBody(p.body()));
        });

        return gitHubLink;
    }

    private void updateOrSave(GitHubLink link) {
        if (gitHubLinkService.findById(link.id()).isPresent()) {
            gitHubLinkService.updateLink(link);
        } else {
            gitHubLinkService.createLink(link);
        }
    }

    private Optional<GitHubIssueDto> getLastIssue(Link link) {
        try {
            List<GitHubIssueDto> issues = fetchIssue(link);
            return issues.isEmpty() ? Optional.empty() : Optional.of(issues.getFirst());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<GitHubPullRequestDto> getLastPullRequest(Link link) {
        try {
            List<GitHubPullRequestDto> pulls = fetchPullRequest(link);
            return pulls.isEmpty() ? Optional.empty() : Optional.of(pulls.getFirst());
        } catch (Exception e) {
            return Optional.empty();
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

    private String formatIssueMessage(GitHubIssueDto issue) {
        return String.format(
                "Появилось новое issue:%nНазвание: %s%nАвтор: %s%nДата создания: %s%nОписание: %s%n%n",
                issue.title(),
                issue.user() != null ? issue.user().username() : "Неизвестный автор",
                issue.createdAt(),
                cutBody(issue.body()));
    }

    private String formatPullRequestMessage(GitHubPullRequestDto pull) {
        return String.format(
                "Появился новый pull request:%nНазвание: %s%nАвтор: %s%nДата создания: %s%nОписание: %s%n%n",
                pull.title(),
                pull.user() != null ? pull.user().username() : "Неизвестный автор",
                pull.createdAt(),
                cutBody(pull.body()));
    }

    private String getOwnerAndRepo(Link link) {
        URI uri = URI.create(link.url());
        String ownerAndRepo = uri.getPath().substring(1);
        return ownerAndRepo;
    }
}
