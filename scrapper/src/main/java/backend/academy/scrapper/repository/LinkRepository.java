package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import java.time.Duration;
import java.util.*;

public interface LinkRepository {
    Link addLink(Long userId, AddLinkRequest linkRequest);

    boolean hasLink(Long userId, String url);

    Link removeLink(Long userId, String url);

    List<Link> getLinks(Long userId);

    List<Link> getAllLinksWithDelay(Duration delay);

    Link updateLink(Link link);
}
