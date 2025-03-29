package backend.academy.scrapper.repository.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.BaseRepositoryTest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(SqlLinkRepository.class)
class LinkRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SqlLinkRepository linkRepository;

    @Test
    @DisplayName("Добавление ссылки: должна добавлять новую ссылку и подписку")
    void addLink_shouldAddLinkAndSubscription() {
        Long userId = 1L;
        AddLinkRequest linkRequest =
                new AddLinkRequest("http://example.com", List.of("tag1", "tag2"), List.of("filter1", "filter2"));

        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?)", userId, OffsetDateTime.now());

        Link addedLink = linkRepository.addLink(userId, linkRequest);

        assertThat(addedLink).isNotNull();
        assertThat(addedLink.url()).isEqualTo(linkRequest.link());
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM subscriptions WHERE link_id = ? AND user_id = ?",
                        Integer.class,
                        addedLink.id(),
                        userId))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM link_tags WHERE link_id = ?", Integer.class, addedLink.id()))
                .isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM link_filters WHERE link_id = ?", Integer.class, addedLink.id()))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Проверка существования ссылки: должна возвращать true, если ссылка существует")
    void hasLink_shouldReturnTrueIfLinkExists() {
        Long userId = 2L;
        String url = "http://existing.com";

        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?)", userId, OffsetDateTime.now());
        jdbcTemplate.update(
                "INSERT INTO links (url, last_updated_at, last_checked_at) VALUES (?, ?, ?)",
                url,
                OffsetDateTime.now(),
                OffsetDateTime.now());

        Long linkId = jdbcTemplate.queryForObject("SELECT id FROM links WHERE url = ?", Long.class, url);

        jdbcTemplate.update("INSERT INTO subscriptions (link_id, user_id) VALUES (?, ?)", linkId, userId);

        boolean exists = linkRepository.hasLink(userId, url);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Удаление ссылки: должна удалять подписку и ссылку, если нет других подписчиков")
    void removeLink_shouldRemoveLinkAndSubscription() {
        Long userId = 3L;
        String url = "http://remove.com";

        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?)", userId, OffsetDateTime.now());

        jdbcTemplate.update("INSERT INTO tags (id, name) VALUES (?, ?)", 1L, "testTag");
        jdbcTemplate.update("INSERT INTO filters (id, name) VALUES (?, ?)", 1L, "testFilter");
        jdbcTemplate.update(
                "INSERT INTO links (url, last_updated_at, last_checked_at) VALUES (?, ?, ?)",
                url,
                OffsetDateTime.now(),
                OffsetDateTime.now());

        Long linkId = jdbcTemplate.queryForObject("SELECT id FROM links WHERE url = ?", Long.class, url);

        jdbcTemplate.update("INSERT INTO subscriptions (link_id, user_id) VALUES (?, ?)", linkId, userId);
        jdbcTemplate.update("INSERT INTO link_tags (link_id, tag_id) VALUES (?, ?)", linkId, 1L);
        jdbcTemplate.update("INSERT INTO link_filters (link_id, filter_id) VALUES (?, ?)", linkId, 1L);

        Link removedLink = linkRepository.removeLink(userId, url);

        assertThat(removedLink).isNotNull();
        assertThat(jdbcTemplate.queryForList(
                        "SELECT * FROM subscriptions WHERE link_id = ? AND user_id = ?", linkId, userId))
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM links WHERE id = ?", linkId))
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM link_tags WHERE link_id = ?", linkId))
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM link_filters WHERE link_id = ?", linkId))
                .isEmpty();
    }

    @Test
    @DisplayName("Получение ссылок пользователя: должен возвращать список ссылок пользователя")
    void getLinks_shouldReturnUserLinks() {
        Long userId = 4L;
        String url1 = "http://userlink1.com";
        String url2 = "http://userlink2.com";

        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?)", userId, OffsetDateTime.now());

        jdbcTemplate.update(
                "INSERT INTO links (url, last_updated_at, last_checked_at) VALUES (?, ?, ?)",
                url1,
                OffsetDateTime.now(),
                OffsetDateTime.now());
        jdbcTemplate.update(
                "INSERT INTO links (url, last_updated_at, last_checked_at) VALUES (?, ?, ?)",
                url2,
                OffsetDateTime.now(),
                OffsetDateTime.now());

        Long linkId1 = jdbcTemplate.queryForObject("SELECT id FROM links WHERE url = ?", Long.class, url1);
        Long linkId2 = jdbcTemplate.queryForObject("SELECT id FROM links WHERE url = ?", Long.class, url2);

        jdbcTemplate.update("INSERT INTO subscriptions (link_id, user_id) VALUES (?, ?)", linkId1, userId);
        jdbcTemplate.update("INSERT INTO subscriptions (link_id, user_id) VALUES (?, ?)", linkId2, userId);

        List<Link> userLinks = linkRepository.getLinks(userId);

        assertThat(userLinks).hasSize(2);
        assertThat(userLinks.stream().map(Link::url)).containsExactlyInAnyOrder(url1, url2);
    }

    @Test
    @DisplayName("Получение ссылок для обновления: должен возвращать ссылки, требующие обновления")
    void getAllLinksWithDelay_shouldReturnLinksForUpdate() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime oldTime = now.minus(Duration.ofDays(1));

        jdbcTemplate.update(
                "INSERT INTO links (url, last_updated_at, last_checked_at) VALUES (?, ?, ?)",
                "http://oldlink.com",
                now,
                oldTime);
        jdbcTemplate.update(
                "INSERT INTO links (url, last_updated_at, last_checked_at) VALUES (?, ?, ?)",
                "http://newlink.com",
                now,
                now);

        List<Link> linksForUpdate = linkRepository.getAllLinksWithDelay(Duration.ofHours(1));

        assertThat(linksForUpdate).hasSize(1);
        assertThat(linksForUpdate.get(0).url()).isEqualTo("http://oldlink.com");
    }

    @Test
    @DisplayName("Обновление ссылки: должно обновлять время последнего обновления")
    void updateLink_shouldUpdateLastUpdatedTime() {
        OffsetDateTime now = OffsetDateTime.now();
        jdbcTemplate.update(
                "INSERT INTO links (url, last_updated_at, last_checked_at) VALUES (?, ?, ?)",
                "http://update.com",
                now.minusHours(1),
                now.minusHours(1));
        Link link = jdbcTemplate.queryForObject(
                "SELECT * FROM links WHERE url = ?",
                (rs, rowNum) -> new Link(
                        rs.getLong("id"),
                        rs.getString("url"),
                        rs.getObject("last_updated_at", OffsetDateTime.class),
                        rs.getObject("last_checked_at", OffsetDateTime.class)),
                "http://update.com");
        link.lastUpdatedAt(now);
        link.lastCheckedAt(now);
        linkRepository.updateLink(link);

        Link updatedLink = jdbcTemplate.queryForObject(
                "SELECT * FROM links WHERE id = ?",
                (rs, rowNum) -> new Link(
                        rs.getLong("id"),
                        rs.getString("url"),
                        rs.getObject("last_updated_at", OffsetDateTime.class),
                        rs.getObject("last_checked_at", OffsetDateTime.class)),
                link.id());

        assertThat(updatedLink.lastUpdatedAt().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(now.truncatedTo(ChronoUnit.SECONDS));
        assertThat(updatedLink.lastCheckedAt().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(now.truncatedTo(ChronoUnit.SECONDS));
    }
}
