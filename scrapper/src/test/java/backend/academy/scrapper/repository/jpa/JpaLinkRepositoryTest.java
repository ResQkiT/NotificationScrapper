package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Tag;
import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.BaseRepositoryTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@Import(JpaLinkRepository.class)
@ActiveProfiles("test")
class JpaLinkRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private JpaLinkRepository linkRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Link link;
    private Tag tag1;
    private Tag tag2;
    private Filter filter1;
    private Filter filter2;

    @BeforeEach
    void setUp() {
        user = new User(1L, OffsetDateTime.now());
        entityManager.persist(user);

        link = new Link("http://example.com", OffsetDateTime.now(), OffsetDateTime.now(), List.of(), List.of());
        entityManager.persist(link);

        tag1 = new Tag("tag1");
        tag2 = new Tag("tag2");
        entityManager.persist(tag1);
        entityManager.persist(tag2);

        filter1 = new Filter("filter1");
        filter2 = new Filter("filter2");
        entityManager.persist(filter1);
        entityManager.persist(filter2);

        link.tags(new ArrayList<>());
        link.filters(new ArrayList<>());
    }

    @Test
    @DisplayName("Добавление ссылки: должен добавить ссылку с тегами и фильтрами")
    void addLink_shouldAddLinkWithTagsAndFilters() {
        AddLinkRequest request =
                new AddLinkRequest("http://test.com", List.of("tag1", "tag2"), List.of("filter1", "filter2"));

        Link addedLink = linkRepository.addLink(user.id(), request);

        assertThat(addedLink.url()).isEqualTo("http://test.com");
        assertThat(addedLink.tags()).containsExactly(tag1, tag2);
        assertThat(addedLink.filters()).containsExactly(filter1, filter2);
        assertThat(user.links()).contains(addedLink);
    }

    @Test
    @DisplayName("Добавление ссылки: должен создать новую ссылку, если она не существует")
    void addLink_shouldCreateNewLinkIfNotExist() {
        AddLinkRequest request = new AddLinkRequest("http://new.com", List.of("tag1"), List.of("filter1"));

        Link addedLink = linkRepository.addLink(user.id(), request);

        assertThat(addedLink.url()).isEqualTo("http://new.com");
        assertThat(entityManager.find(Link.class, addedLink.id())).isNotNull();
    }

    @Test
    @DisplayName("Добавление ссылки: должен использовать существующие теги и фильтры")
    void addLink_shouldUseExistingTagsAndFilters() {
        AddLinkRequest request = new AddLinkRequest("http://example.com", List.of("tag1"), List.of("filter1"));

        Link addedLink = linkRepository.addLink(user.id(), request);

        assertThat(addedLink.tags()).containsExactly(tag1);
        assertThat(addedLink.filters()).containsExactly(filter1);
    }

    @Test
    @DisplayName("Добавление ссылки: должен выбрасывать EntityNotFoundException, если пользователь не найден")
    void addLink_shouldThrowExceptionIfUserNotFound() {
        AddLinkRequest request = new AddLinkRequest("http://test.com", List.of("tag1"), List.of("filter1"));

        assertThrows(EntityNotFoundException.class, () -> linkRepository.addLink(2L, request));
    }

    @Test
    @DisplayName("Проверка наличия ссылки: должен возвращать true, если ссылка существует")
    void hasLink_shouldReturnTrueIfLinkExists() {
        user.links().add(link);
        link.users().add(user);
        entityManager.merge(user);

        boolean hasLink = linkRepository.hasLink(user.id(), "http://example.com");

        assertThat(hasLink).isTrue();
    }

    @Test
    @DisplayName("Проверка наличия ссылки: должен возвращать false, если ссылка не существует")
    void hasLink_shouldReturnFalseIfLinkNotExist() {
        boolean hasLink = linkRepository.hasLink(user.id(), "http://nonexistent.com");

        assertThat(hasLink).isFalse();
    }

    @Test
    @DisplayName("Удаление ссылки: должен удалить ссылку и связи с пользователем")
    void removeLink_shouldRemoveLinkAndUserRelation() {
        user.links().add(link);
        entityManager.merge(user);

        linkRepository.removeLink(user.id(), "http://example.com");

        assertThat(user.links()).isEmpty();
        assertThat(link.users()).isEmpty();
        assertThat(entityManager.find(Link.class, link.id())).isNull();
    }

    @Test
    @DisplayName("Удаление ссылки: должен выбрасывать EntityNotFoundException, если пользователь не найден")
    void removeLink_shouldThrowExceptionIfUserNotFound() {
        assertThrows(EntityNotFoundException.class, () -> linkRepository.removeLink(2L, "http://example.com"));
    }

    @Test
    @DisplayName("Удаление ссылки: должен выбрасывать EntityNotFoundException, если ссылка не найдена")
    void removeLink_shouldThrowExceptionIfLinkNotFound() {
        assertThrows(
                EntityNotFoundException.class, () -> linkRepository.removeLink(user.id(), "http://nonexistent.com"));
    }

    @Test
    @DisplayName("Получение ссылок пользователя: должен возвращать список ссылок с тегами и фильтрами")
    void getLinks_shouldReturnListOfLinksWithTagsAndFilters() {
        user.links().add(link);
        link.tags().add(tag1);
        link.users().add(user);
        link.filters().add(filter1);
        entityManager.merge(user);
        entityManager.merge(link);

        List<Link> links = linkRepository.getLinks(user.id());

        assertThat(links).hasSize(1);
        assertThat(links.get(0).url()).isEqualTo("http://example.com");
        assertThat(links.get(0).tags()).containsExactly(tag1);
        assertThat(links.get(0).filters()).containsExactly(filter1);
    }

    @Test
    @DisplayName("Обновление ссылки: должен обновить ссылку")
    void updateLink_shouldUpdateLink() {
        link.url("http://updated.com");
        Link updatedLink = linkRepository.updateLink(link);

        assertThat(updatedLink.url()).isEqualTo("http://updated.com");
    }
}
