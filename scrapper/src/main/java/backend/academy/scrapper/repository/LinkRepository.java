package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.entity.Link;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class LinkRepository {
    private static AtomicLong id = new AtomicLong();

    private final Map<Long, List<Link>> userLinks = new ConcurrentHashMap<>();

    public Link addLink(Long userId, AddLinkRequest linkRequest) {
        long newId = id.incrementAndGet();
        Link link = new Link(newId, linkRequest.link(), linkRequest.tags(), linkRequest.filters());
        link.chatsId().add(userId);
        userLinks.computeIfAbsent(userId, k -> new ArrayList<>()).add(link);
        return link;
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

    public Link updateLink(Link link) {
        List<Long> chats = link.chatsId();

        for (Long chatId : chats) {
            userLinks.computeIfAbsent(chatId, k -> new ArrayList<>());
            userLinks.get(chatId).removeIf(existingLink -> existingLink.url().equals(link.url()));
            userLinks.get(chatId).add(link);
        }

        return link;
    }
}
