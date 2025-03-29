package backend.academy.scrapper.repository.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.StackOverflowLink;
import backend.academy.scrapper.repository.BaseRepositoryTest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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
@Import({SqlStackoverflowLinkRepository.class, SqlLinkRepository.class})
class StackoverflowLinkRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private SqlStackoverflowLinkRepository stackoverflowLinkRepository;

    @Test
    @DisplayName("Сохранение StackOverflowLink: должен сохранять ссылку в базу данных")
    void save_shouldSaveStackoverflowLink() {
        StackOverflowLink link = new StackOverflowLink();
        link.id(1L);
        link.url("http://stackoverflow.com/test");
        link.answerLastId(10L);
        link.answerLastUsername("testUser");
        link.answerCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.answerPreviewDescription("Test answer description");
        link.commentId(20L);
        link.commentLastUsername("testUser");
        link.commentCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.commentPreviewDescription("Test comment description");

        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                1L,
                "http://stackoverflow.com/test",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        stackoverflowLinkRepository.save(link);

        StackOverflowLink retrievedLink = jdbcTemplate.queryForObject(
                "SELECT l.id, l.url, sl.* FROM links l JOIN stackoverflow_links sl ON l.id = sl.link_id WHERE l.id = ?",
                (rs, rowNum) -> {
                    StackOverflowLink result = new StackOverflowLink();
                    result.id(rs.getLong("id"));
                    result.url(rs.getString("url"));
                    result.answerLastId(rs.getLong("answer_last_id"));
                    result.answerLastUsername(rs.getString("answer_last_username"));
                    result.answerCreatedAt(rs.getObject("answer_created_at", OffsetDateTime.class)
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .truncatedTo(ChronoUnit.SECONDS));
                    result.answerPreviewDescription(rs.getString("answer_preview_description"));
                    result.commentId(rs.getLong("comment_id"));
                    result.commentLastUsername(rs.getString("comment_last_username"));
                    result.commentCreatedAt(rs.getObject("comment_created_at", OffsetDateTime.class)
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .truncatedTo(ChronoUnit.SECONDS));
                    result.commentPreviewDescription(rs.getString("comment_preview_description"));
                    return result;
                },
                1L);

        assertThat(retrievedLink).isNotNull();
        assertThat(retrievedLink.id()).isEqualTo(link.id());
        assertThat(retrievedLink.answerLastId()).isEqualTo(link.answerLastId());
        assertThat(retrievedLink.answerLastUsername()).isEqualTo(link.answerLastUsername());
        assertThat(retrievedLink.answerCreatedAt()).isEqualTo(link.answerCreatedAt());
        assertThat(retrievedLink.answerPreviewDescription()).isEqualTo(link.answerPreviewDescription());
        assertThat(retrievedLink.commentId()).isEqualTo(link.commentId());
        assertThat(retrievedLink.commentLastUsername()).isEqualTo(link.commentLastUsername());
        assertThat(retrievedLink.commentCreatedAt()).isEqualTo(link.commentCreatedAt());
        assertThat(retrievedLink.commentPreviewDescription()).isEqualTo(link.commentPreviewDescription());
    }

    @Test
    @DisplayName("Обновление StackOverflowLink: должен обновлять ссылку в базе данных")
    void update_shouldUpdateStackoverflowLink() {
        StackOverflowLink link = new StackOverflowLink();
        link.id(2L);
        link.url("http://stackoverflow.com/test");
        link.answerLastId(10L);
        link.answerLastUsername("testUser");
        link.answerCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.answerPreviewDescription("Test answer description");
        link.commentId(20L);
        link.commentLastUsername("testUser");
        link.commentCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.commentPreviewDescription("Test comment description");

        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                2L,
                "http://stackoverflow.com/test",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        stackoverflowLinkRepository.save(link);

        link.answerPreviewDescription("Updated answer description");
        link.commentPreviewDescription("Updated comment description");

        stackoverflowLinkRepository.update(link);

        StackOverflowLink updatedLink = jdbcTemplate.queryForObject(
                "SELECT l.id, l.url, sl.* FROM links l JOIN stackoverflow_links sl ON l.id = sl.link_id WHERE l.id = ?",
                (rs, rowNum) -> {
                    StackOverflowLink result = new StackOverflowLink();
                    result.id(rs.getLong("id"));
                    result.url(rs.getString("url"));
                    result.answerLastId(rs.getLong("answer_last_id"));
                    result.answerLastUsername(rs.getString("answer_last_username"));
                    result.answerCreatedAt(rs.getObject("answer_created_at", OffsetDateTime.class)
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .truncatedTo(ChronoUnit.SECONDS));
                    result.answerPreviewDescription(rs.getString("answer_preview_description"));
                    result.commentId(rs.getLong("comment_id"));
                    result.commentLastUsername(rs.getString("comment_last_username"));
                    result.commentCreatedAt(rs.getObject("comment_created_at", OffsetDateTime.class)
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .truncatedTo(ChronoUnit.SECONDS));
                    result.commentPreviewDescription(rs.getString("comment_preview_description"));
                    return result;
                },
                2L);

        assertThat(updatedLink).isNotNull();
        assertThat(updatedLink.answerPreviewDescription()).isEqualTo("Updated answer description");
        assertThat(updatedLink.commentPreviewDescription()).isEqualTo("Updated comment description");
    }

    @Test
    @DisplayName("Поиск StackOverflowLink по ID родительской ссылки: должен возвращать ссылку, если она существует")
    void findByParentId_shouldReturnStackoverflowLinkIfExists() {
        StackOverflowLink link = new StackOverflowLink();
        link.id(3L);
        link.url("http://stackoverflow.com/test");
        link.answerLastId(10L);
        link.answerLastUsername("testUser");
        link.answerCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.answerPreviewDescription("Test answer description");
        link.commentId(20L);
        link.commentLastUsername("testUser");
        link.commentCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.commentPreviewDescription("Test comment description");

        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                3L,
                "http://stackoverflow.com/test",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        stackoverflowLinkRepository.save(link);

        Optional<StackOverflowLink> foundLink = stackoverflowLinkRepository.findByParentId(3L);

        assertThat(foundLink).isPresent();
        assertThat(foundLink.get().id()).isEqualTo(link.id());
        assertThat(foundLink.get().answerLastId()).isEqualTo(link.answerLastId());
        assertThat(foundLink.get().answerLastUsername()).isEqualTo(link.answerLastUsername());
        assertThat(foundLink.get().answerCreatedAt()).isEqualTo(link.answerCreatedAt());
        assertThat(foundLink.get().answerPreviewDescription()).isEqualTo(link.answerPreviewDescription());
        assertThat(foundLink.get().commentId()).isEqualTo(link.commentId());
        assertThat(foundLink.get().commentLastUsername()).isEqualTo(link.commentLastUsername());
        assertThat(foundLink.get().commentCreatedAt()).isEqualTo(link.commentCreatedAt());
        assertThat(foundLink.get().commentPreviewDescription()).isEqualTo(link.commentPreviewDescription());
    }

    @Test
    @DisplayName("Удаление StackOverflowLink по ID родительской ссылки: должен удалять ссылку из базы данных")
    void deleteByParentId_shouldDeleteStackoverflowLink() {
        StackOverflowLink link = new StackOverflowLink();
        link.id(4L);
        link.url("http://stackoverflow.com/test");
        link.answerLastId(10L);
        link.answerLastUsername("testUser");
        link.answerCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.answerPreviewDescription("Test answer description");
        link.commentId(20L);
        link.commentLastUsername("testUser");
        link.commentCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.commentPreviewDescription("Test comment description");

        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                4L,
                "http://stackoverflow.com/test",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        stackoverflowLinkRepository.save(link);

        stackoverflowLinkRepository.deleteByParentId(4L);

        Optional<StackOverflowLink> deletedLink = stackoverflowLinkRepository.findByParentId(4L);

        assertThat(deletedLink).isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM links WHERE id = ?", 4L))
                .isEmpty();
    }

    @Test
    @DisplayName("Получение всех StackOverflowLink: должен возвращать список всех ссылок")
    void findAll_shouldReturnAllStackoverflowLinks() {
        StackOverflowLink link1 = new StackOverflowLink();
        link1.id(5L);
        link1.url("http://stackoverflow.com/test1");
        link1.answerLastId(10L);
        link1.answerLastUsername("testUser1");
        link1.answerCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link1.answerPreviewDescription("Test answer description 1");
        link1.commentId(20L);
        link1.commentLastUsername("testUser1");
        link1.commentCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link1.commentPreviewDescription("Test comment description 1");

        StackOverflowLink link2 = new StackOverflowLink();
        link2.id(6L);
        link2.url("http://stackoverflow.com/test2");
        link2.answerLastId(11L);
        link2.answerLastUsername("testUser2");
        link2.answerCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link2.answerPreviewDescription("Test answer description 2");
        link2.commentId(21L);
        link2.commentLastUsername("testUser2");
        link2.commentCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link2.commentPreviewDescription("Test comment description 2");

        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                5L,
                "http://stackoverflow.com/test1",
                OffsetDateTime.now(),
                OffsetDateTime.now());
        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                6L,
                "http://stackoverflow.com/test2",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        stackoverflowLinkRepository.save(link1);
        stackoverflowLinkRepository.save(link2);

        List<StackOverflowLink> allLinks = stackoverflowLinkRepository.findAll();

        assertThat(allLinks).hasSize(2);
        assertThat(allLinks.get(0).id()).isEqualTo(link1.id());
        assertThat(allLinks.get(1).id()).isEqualTo(link2.id());
    }
}
