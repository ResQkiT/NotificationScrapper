package backend.academy.scrapper.service;

import backend.academy.scrapper.model.StackOverflowLink;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.sql.SqlStackoverflowLinkRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StackOverflowLinkService {
    private final SqlStackoverflowLinkRepository repository;

    @Transactional
    public Link createLink(StackOverflowLink link) {
        return repository.save(link);
    }

    @Transactional
    public StackOverflowLink updateLink(StackOverflowLink link) {
        return repository.update(link);
    }

    @Transactional(readOnly = true)
    public Optional<StackOverflowLink> findById(Long id) {
        return repository.findByParentId(id);
    }

    @Transactional
    public void deleteLink(Long id) {
        repository.deleteByParentId(id);
    }

    @Transactional(readOnly = true)
    public List<StackOverflowLink> findAllLinks() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsByUrl(String url) {
        return repository.findAll().stream()
            .anyMatch(link -> link.url().equals(url));
    }
}
