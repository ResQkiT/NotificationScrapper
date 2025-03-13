package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkService {

    private final LinkRepository linkRepository;

    @Autowired
    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public List<Link> getAllLinks(Long id) {
        return linkRepository.getLinks(id);
    }

    public boolean hasLink(Long id, AddLinkRequest linkRequest) {
        return linkRepository.hasLink(id, linkRequest);
    }

    public Link addLink(Long id, AddLinkRequest linkRequest) {
        return linkRepository.addLink(id, linkRequest);
    }

    public Link removeLink(Long chatId, String url) {
        var linkToRemove = linkRepository.getLinks(chatId).stream()
                .filter(l -> l.url().equals(url))
                .findFirst();

        linkToRemove.ifPresent(l -> linkRepository.removeLink(chatId, l.url()));
        return linkToRemove.orElse(null);
    }

    public Link updateLink(Link link) {
        return linkRepository.updateLink(link);
    }
}
