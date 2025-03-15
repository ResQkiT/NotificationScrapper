package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Tag;
import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.UserRepository;
import backend.academy.scrapper.repository.jpa.JpaFilterRepository;
import backend.academy.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.scrapper.repository.jpa.JpaTagRepository;
import backend.academy.scrapper.repository.jpa.JpaUserRepository;
import backend.academy.scrapper.service.LinkService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@ConditionalOnProperty(name = "db.access-type", havingValue = "JPA")
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaTagRepository tagRepository;
    private final JpaFilterRepository filterRepository;
    private final JpaUserRepository userRepository;

    @Override
    public boolean hasLink(Long userId, String url) {
        return linkRepository.existsByUrlAndUsersId(url, userId);
    }

    @Override
    public List<Link> getAllLinks(Long userId) {
        return linkRepository.findAllByUsersId(userId).stream()
            .peek(link -> {
                // Инициализируем ленивые коллекции внутри транзакции
                Hibernate.initialize(link.getTags());
                Hibernate.initialize(link.getFilters());
            })
            .collect(Collectors.toList());
    }

    @Override
    public Link removeLink(Long userId, String url) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Link link = linkRepository.findByUrl(url)
            .orElseThrow(() -> new EntityNotFoundException("Link not found"));

        // Удаляем связь пользователя со ссылкой
        user.links().remove(link);
        link.users().remove(user);

        // Если у ссылки больше нет пользователей - удаляем полностью
        if(link.users().isEmpty()) {
            // Удаляем связи с тегами и фильтрами
            link.tags().forEach(tag -> tag.links().remove(link));
            link.filters().forEach(filter -> filter.links().remove(link));

            // Удаляем саму ссылку
            linkRepository.delete(link);

            // Очищаем неиспользуемые теги и фильтры
            cleanUnusedTagsAndFilters(link);
        } else {
            linkRepository.save(link);
        }

        userRepository.save(user);
        return link;
    }

    private void cleanUnusedTagsAndFilters(Link link) {
        link.tags().stream()
            .filter(tag -> tag.links().isEmpty())
            .forEach(tagRepository::delete);

        link.filters().stream()
            .filter(filter -> filter.links().isEmpty())
            .forEach(filterRepository::delete);
    }
    public Link addLink(Long userId, AddLinkRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Link link = linkRepository.findByUrl(request.link())
            .orElseGet(() -> createNewLink(request));

        if (!link.users().contains(user)) {
                link.users().add(user);
            user.links().add(link);
        }

        processTags(link, request.tags());
        processFilters(link, request.filters());

        return linkRepository.save(link);
    }

    private Link createNewLink(AddLinkRequest request) {
        Link newLink = new Link(request.link());
        newLink.lastCheckedAt(LocalDateTime.now());
        return newLink;
    }

    private void processTags(Link link, List<String> tagNames) {
        tagNames.forEach(tagName -> {
            Tag tag = tagRepository.findByName(tagName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName)));
            if (!link.tags().contains(tag)) {
                link.tags().add(tag);
                tag.links().add(link);
            }
        });
    }

    private void processFilters(Link link, List<String> filterNames) {
        filterNames.forEach(filterName -> {
            Filter filter = filterRepository.findByName(filterName)
                .orElseGet(() -> filterRepository.save(new Filter(filterName)));
            if (!link.filters().contains(filter)) {
                link.filters().add(filter);
                filter.links().add(link);
            }
        });
    }

    public List<Link> getAllLinksForUser(Long userId) {
        return linkRepository.findAllByUsersId(userId);
    }

    public Link updateLink(Link link) {
        link.lastCheckedAt(LocalDateTime.now());
        return linkRepository.save(link);
    }


}
