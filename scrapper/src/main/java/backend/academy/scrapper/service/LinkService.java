package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public boolean hasLink(Long id, String linkUrl) {
        return linkRepository.hasLink(id, linkUrl);
    }

    public List<Link> getAllLinks(Long id) {
        return linkRepository.getLinks(id);
    }

    public List<Link> getAllLinksWithDelay(Duration delay) {
        return linkRepository.getAllLinksWithDelay(delay);
    }

    public Link addLink(Long id, AddLinkRequest linkRequest) {
        return linkRepository.addLink(id, linkRequest);
    }

    public Link updateLink(Link link) {
        return linkRepository.updateLink(link);
    }

    public Link removeLink(Long chatId, String url) {
        return linkRepository.removeLink(chatId, url);
    }

    @Transactional
    public Link onTouchLink(Link link) {
        Link managedLink = linkRepository
                .findLinkById(link.id())
                .orElseThrow(() -> new EntityNotFoundException("Link not found with id: " + link.id()));

        managedLink.lastCheckedAt(OffsetDateTime.now());

        updateLink(managedLink);
        return managedLink;
    }

    @Transactional
    public Link onUpdateLink(Link link) {
        Link managedLink = linkRepository
                .findLinkById(link.id())
                .orElseThrow(() -> new EntityNotFoundException("Link not found with id: " + link.id()));

        managedLink.lastUpdatedAt(OffsetDateTime.now());

        updateLink(managedLink);
        return managedLink;
    }
}
