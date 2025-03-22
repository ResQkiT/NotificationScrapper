package backend.academy.scrapper.repository;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Tag;
import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.sql.SqlLinkRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkRepositoryTest {

    private LinkRepository repository;

    @BeforeEach
    public void setup() {
        repository = new SqlLinkRepository();
    }

    @Test
    @DisplayName("Добавление ссылки: при валидных данных ссылка добавляется и возвращается")
    public void testAddLink_whenValidInput_thenReturnLinkAndStoreIt() {
        Long userId = 1L;
        AddLinkRequest request = new AddLinkRequest("http://example.com", List.of("tag1"), List.of("filter1"));
        Link link = repository.addLink(userId, request);
        assertNotNull(link);
        assertEquals("http://example.com", link.url());
        assertTrue(link.users().contains(new User(1L)));
        List<Link> links = repository.getLinks(userId);
        assertEquals(1, links.size());
        assertEquals(link, links.get(0));
    }

    @Test
    @DisplayName("Получение ссылок: если ссылок нет, возвращается пустой список")
    public void testGetLinks_whenNoLinks_thenReturnEmptyList() {
        Long userId = 2L;
        List<Link> links = repository.getLinks(userId);
        assertNotNull(links);
        assertTrue(links.isEmpty());
    }

    @Test
    @DisplayName("Удаление ссылки: если ссылка существует, она удаляется и возвращается true")
    public void testRemoveLink_whenLinkExists_thenReturnTrueAndRemoveLink() {
        Long userId = 3L;
        AddLinkRequest request = new AddLinkRequest("http://example.com", List.of("tag1"), List.of("filter1"));
        repository.addLink(userId, request);
        boolean removed = repository.removeLink(userId, "http://example.com");
        assertTrue(removed);
        List<Link> links = repository.getLinks(userId);
        assertTrue(links.isEmpty());
    }

    @Test
    @DisplayName("Удаление ссылки: если ссылка не существует, возвращается false")
    public void testRemoveLink_whenLinkDoesNotExist_thenReturnFalse() {
        Long userId = 4L;
        boolean removed = repository.removeLink(userId, "http://nonexistent.com");
        assertFalse(removed);
    }

    @Test
    @DisplayName("Обновление ссылки: если ссылка существует, она обновляется для всех чатов")
    public void testUpdateLink_whenLinkExists_thenUpdateLinkForAllChats() {
        Long userId = 5L;
        AddLinkRequest request = new AddLinkRequest("http://example.com", List.of("tag1"), List.of("filter1"));
        Link originalLink = repository.addLink(userId, request);
        Link updatedLink = new Link(
                originalLink.id(),
                originalLink.url(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                List.of(new Tag("tag1")),
                List.of(new Filter("filter1")));
        updatedLink.users().add(new User(userId));
        Link resultLink = repository.updateLink(updatedLink);
        assertNotNull(resultLink);
        assertEquals("http://example.com", resultLink.url());
        assertEquals(List.of("updatedTag"), resultLink.tags());
        assertEquals(List.of("updatedFilter"), resultLink.filters());
        List<Link> links = repository.getLinks(userId);
        assertEquals(1, links.size());
        Link storedLink = links.get(0);
        assertEquals("http://example.com", storedLink.url());
        assertEquals(List.of("updatedTag"), storedLink.tags());
        assertEquals(List.of("updatedFilter"), storedLink.filters());
    }
}
