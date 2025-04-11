package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.StackOverflowLink;
import backend.academy.scrapper.repository.sql.SqlStackoverflowLinkRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StackOverflowLinkService {
    private final SqlStackoverflowLinkRepository stackoverflowLinkRepository;
    private final LinkService linkService;

    @Autowired
    public StackOverflowLinkService(
            SqlStackoverflowLinkRepository stackoverflowLinkRepository, LinkService linkService) {
        this.stackoverflowLinkRepository = stackoverflowLinkRepository;
        this.linkService = linkService;
    }

    public Link createLink(StackOverflowLink link) {
        return stackoverflowLinkRepository.save(link);
    }

    public StackOverflowLink updateLink(StackOverflowLink link) {
        return stackoverflowLinkRepository.update(link);
    }

    public Optional<StackOverflowLink> findById(Long id) {
        return stackoverflowLinkRepository.findByParentId(id);
    }

    public void deleteLink(Long id) {
        stackoverflowLinkRepository.deleteByParentId(id);
    }

    public List<StackOverflowLink> findAllLinks() {
        return stackoverflowLinkRepository.findAll();
    }

    public boolean existsByUrl(String url) {
        return stackoverflowLinkRepository.findAll().stream()
                .anyMatch(link -> link.url().equals(url));
    }
}
