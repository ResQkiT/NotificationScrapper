package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LinkRepository {
    private final Map<Long, List<Link>> userLinks = new ConcurrentHashMap<>();

    public void addLink(Long userId, Link link) {
        userLinks.computeIfAbsent(userId, k -> new ArrayList<>()).add(link);
    }

    public boolean removeLink(Long userId, String url) {
        List<Link> links = userLinks.get(userId);
        if (links != null) {
            boolean removed = links.removeIf(link -> link.url().equals(url));
            if (links.isEmpty()) {
                userLinks.remove(userId);
            }
            return removed;
        }
        return false;
    }

    public List<Link> getLinks(Long userId) {
        return userLinks.getOrDefault(userId, Collections.emptyList());
    }
}
