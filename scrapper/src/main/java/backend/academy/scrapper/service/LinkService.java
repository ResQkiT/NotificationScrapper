package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.sql.SqlLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LinkService implements ILinkService {

    private final LinkRepository linkRepository;

    @Autowired
    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    public boolean hasLink(Long id, String linkUrl) {
        return linkRepository.hasLink(id, linkUrl);
    }

    @Override
    public List<Link> getAllLinks(Long id) {
        return linkRepository.getLinks(id);
    }

    @Override
    public Link addLink(Long id, AddLinkRequest linkRequest) {
        return linkRepository.addLink(id, linkRequest);
    }

    @Override
    public Link updateLink(Link link) {
        return linkRepository.updateLink(link);
    }

    @Override
    public Link removeLink(Long chatId, String url) {
        return linkRepository.removeLink(chatId, url);
    }
}
