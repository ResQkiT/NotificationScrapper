package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ILinkService {

    boolean hasLink(Long id, String linkUrl);

    List<Link> getAllLinks(Long id);

    List<Link> getAllLinksWithDelay(Duration delay);

    Link addLink(Long id, AddLinkRequest linkRequest);

    Link updateLink(Link link);

    Link removeLink(Long chatId, String url);
}
