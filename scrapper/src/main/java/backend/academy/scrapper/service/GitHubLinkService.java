package backend.academy.scrapper.service;

import backend.academy.scrapper.model.GitHubLink;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.sql.SqlGithubLinkRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GitHubLinkService {
    private final SqlGithubLinkRepository githubLinkRepository;
    private final LinkService linkService;

    @Autowired
    public GitHubLinkService(SqlGithubLinkRepository githubLinkRepository, LinkService linkService) {
        this.githubLinkRepository = githubLinkRepository;
        this.linkService = linkService;
    }

    public Link createLink(GitHubLink link) {
        return githubLinkRepository.save(link);
    }

    public GitHubLink updateLink(GitHubLink link) {
        return githubLinkRepository.update(link);
    }

    public Optional<GitHubLink> findById(Long id) {
        return githubLinkRepository.findByParentId(id);
    }

    public void deleteLink(Long id) {
        githubLinkRepository.deleteByParentId(id);
    }

    public List<GitHubLink> findAllLinks() {
        return githubLinkRepository.findAll();
    }

    public Long countLinks() {
        return githubLinkRepository.count();
    }

    public boolean existsByUrl(String url) {
        return githubLinkRepository.findAll().stream()
                .anyMatch(link -> link.url().equals(url));
    }
}
