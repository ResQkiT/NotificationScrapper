package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import java.util.*;

public interface LinkRepository {
    Link addLink(Long userId, AddLinkRequest linkRequest);
    boolean hasLink(Long userId, String url);
    Link removeLink(Long userId, String url);
    List<Link> getLinks(Long userId);
    Link updateLink(Link link);
}
