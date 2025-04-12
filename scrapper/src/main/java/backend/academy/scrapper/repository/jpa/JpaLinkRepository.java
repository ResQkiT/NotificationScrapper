package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Tag;
import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.LinkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@ConditionalOnProperty(name = "db.access-type", havingValue = "JPA")
public class JpaLinkRepository implements LinkRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Link addLink(Long userId, AddLinkRequest linkRequest) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }

        Link link = entityManager
                .createQuery("SELECT l FROM Link l WHERE l.url = :url", Link.class)
                .setParameter("url", linkRequest.link())
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    Link newLink = new Link(
                            linkRequest.link(),
                            OffsetDateTime.now(),
                            OffsetDateTime.now(),
                            new ArrayList<>(),
                            new ArrayList<>());
                    entityManager.persist(newLink);
                    return newLink;
                });

        List<Tag> tags = linkRequest.tags().stream()
                .map(tagName -> entityManager
                        .createQuery("SELECT t FROM Tag t WHERE t.name = :name", Tag.class)
                        .setParameter("name", tagName)
                        .getResultStream()
                        .findFirst()
                        .orElseGet(() -> {
                            Tag newTag = new Tag(tagName);
                            entityManager.persist(newTag);
                            return newTag;
                        }))
                .toList();

        List<Filter> filters = linkRequest.filters().stream()
                .map(filterName -> entityManager
                        .createQuery("SELECT f FROM Filter f WHERE f.name = :name", Filter.class)
                        .setParameter("name", filterName)
                        .getResultStream()
                        .findFirst()
                        .orElseGet(() -> {
                            Filter newFilter = new Filter(filterName);
                            entityManager.persist(newFilter);
                            return newFilter;
                        }))
                .toList();

        if (!user.links().contains(link)) {
            user.links().add(link);
            link.users().add(user);
        }

        link.tags(new ArrayList<>(tags));
        link.filters(new ArrayList<>(filters));

        return link;
    }

    @Override
    public boolean hasLink(Long userId, String url) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(l) FROM Link l JOIN l.users u WHERE u.id = :userId AND l.url = :url", Long.class)
                .setParameter("userId", userId)
                .setParameter("url", url)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public Link removeLink(Long userId, String url) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }

        String jpql = "SELECT l FROM Link l LEFT JOIN FETCH l.users u WHERE l.url = :url";
        Link link = entityManager
            .createQuery(jpql, Link.class)
            .setParameter("url", url)
            .getResultStream()
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Link not found with URL: " + url));

        boolean removedFromUser = user.links().remove(link);

        link.users().remove(user);

        if (removedFromUser) {

            if (link.users().isEmpty()) {
                removeOrphanedTagsAndFilters(link);
                entityManager.remove(link);
            }
        }
        Hibernate.initialize(link.tags());
        Hibernate.initialize(link.filters());
        return link;
    }

    @Override
    public List<Link> getLinks(Long userId) {
        List<Link> links = entityManager
                .createQuery("SELECT DISTINCT l FROM Link l JOIN l.users u WHERE u.id = :userId", Link.class)
                .setParameter("userId", userId)
                .getResultList();

        if (links.isEmpty()) {
            return links;
        }

        List<Link> linksWithUsers = entityManager
                .createQuery("SELECT DISTINCT l FROM Link l LEFT JOIN FETCH l.users u WHERE l IN :links", Link.class)
                .setParameter("links", links)
                .getResultList();

        List<Link> linksWithTags = entityManager
                .createQuery("SELECT DISTINCT l FROM Link l LEFT JOIN FETCH l.tags t WHERE l IN :links", Link.class)
                .setParameter("links", links)
                .getResultList();

        List<Link> linksWithFilters = entityManager
                .createQuery("SELECT DISTINCT l FROM Link l LEFT JOIN FETCH l.filters f WHERE l IN :links", Link.class)
                .setParameter("links", links)
                .getResultList();

        return links;
    }

    @Override
    public List<Link> getAllLinksWithDelay(Duration delay) {
        OffsetDateTime threshold = OffsetDateTime.now().minus(delay);
        String initialJpql =
                """
                            SELECT DISTINCT l FROM Link l
                            WHERE l.lastCheckedAt <= :threshold OR l.lastCheckedAt IS NULL
                            """;

        List<Link> links = entityManager
                .createQuery(initialJpql, Link.class)
                .setParameter("threshold", threshold)
                .getResultList();

        if (links.isEmpty()) {
            return links;
        }

        List<Link> linksWithUsers = entityManager
                .createQuery("SELECT DISTINCT l FROM Link l LEFT JOIN FETCH l.users u WHERE l IN :links", Link.class)
                .setParameter("links", links)
                .getResultList();

        List<Link> linksWithTags = entityManager
                .createQuery("SELECT DISTINCT l FROM Link l LEFT JOIN FETCH l.tags t WHERE l IN :links", Link.class)
                .setParameter("links", links)
                .getResultList();

        List<Link> linksWithFilters = entityManager
                .createQuery("SELECT DISTINCT l FROM Link l LEFT JOIN FETCH l.filters f WHERE l IN :links", Link.class)
                .setParameter("links", links)
                .getResultList();

        return links;
    }

    @Override
    public Link updateLink(Link link) {
        return entityManager.merge(link);
    }

    @Override
    public Optional<Link> findLinkById(Long linkId) {
        return Optional.ofNullable(entityManager.find(Link.class, linkId));
    }

    private void removeOrphanedTagsAndFilters(Link link) {
        List<Tag> orphanedTags = link.tags().stream()
                .filter(tag -> {
                    Long count = entityManager
                            .createQuery("SELECT COUNT(l) FROM Link l JOIN l.tags t WHERE t.id = :tagId", Long.class)
                            .setParameter("tagId", tag.id())
                            .getSingleResult();
                    return count <= 1;
                })
                .toList();

        List<Filter> orphanedFilters = link.filters().stream()
                .filter(filter -> {
                    Long count = entityManager
                            .createQuery(
                                    "SELECT COUNT(l) FROM Link l JOIN l.filters f WHERE f.id = :filterId", Long.class)
                            .setParameter("filterId", filter.id())
                            .getSingleResult();
                    return count <= 1;
                })
                .toList();

        orphanedTags.forEach(entityManager::remove);
        orphanedFilters.forEach(entityManager::remove);
    }
}
