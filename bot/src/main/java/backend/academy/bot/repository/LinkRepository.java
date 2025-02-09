package backend.academy.bot.repository;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class LinkRepository {

    private final Map<Long, List<String>> userLinks = new HashMap<>();

    public void addLink(Long userId, String url) {
        userLinks.computeIfAbsent(userId, k -> new ArrayList<>()).add(url);
    }

    public boolean removeLink(Long userId, String url) {
        List<String> links = userLinks.get(userId);
        if (links != null) {
            boolean removed = links.remove(url);
            if (links.isEmpty()) {
                userLinks.remove(userId);
            }
            return removed;
        }
        return false;
    }

    public List<String> getLinks(Long userId) {
        return userLinks.getOrDefault(userId, Collections.emptyList());
    }

}
