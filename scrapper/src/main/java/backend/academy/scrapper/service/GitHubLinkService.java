package backend.academy.scrapper.service;

import backend.academy.scrapper.model.GitHubLink;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.sql.SqlGithubLinkRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GitHubLinkService {
    private final SqlGithubLinkRepository repository;

    @Transactional
    public Link createLink(GitHubLink link) {
        return repository.save(link);
    }

    @Transactional
    public GitHubLink updateLink(GitHubLink link) {
        return repository.update(link);
    }

    @Transactional(readOnly = true)
    public Optional<GitHubLink> findById(Long id) {
        return repository.findByParentId(id);
    }

    @Transactional
    public void deleteLink(Long id) {
        repository.deleteByParentId(id);
    }

    @Transactional(readOnly = true)
    public List<GitHubLink> findAllLinks() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsByUrl(String url) {
        return repository.findAll().stream().anyMatch(link -> link.url().equals(url));
    }
}
