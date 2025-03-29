package backend.academy.scrapper.repository.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.GitHubLink;
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
@Import({SqlGithubLinkRepository.class, SqlLinkRepository.class})
class GithubLinkRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SqlGithubLinkRepository githubLinkRepository;

    @Test
    @DisplayName("Сохранение GitHubLink: должен сохранять ссылку в базу данных")
    void save_shouldSaveGithubLink() {
        GitHubLink link = new GitHubLink();
        link.id(1L);
        link.url("http://github.com/test");
        link.lastIssueId(10L);
        link.lastIssueTitle("Test Issue");
        link.issueCreatorUsername("testUser");
        link.issueCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.issuePreviewDescription("Test issue description");
        link.lastPullRequestId(20L);
        link.lastPullRequestTitle("Test Pull Request");
        link.pullCreatorUsername("testUser");
        link.pullCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.pullPreviewDescription("Test pull request description");

        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                1L,
                "http://github.com/test",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        githubLinkRepository.save(link);

        GitHubLink retrievedLink = jdbcTemplate.queryForObject(
                "SELECT l.id, l.url, gl.* FROM links l JOIN github_links gl ON l.id = gl.link_id WHERE l.id = ?",
                (rs, rowNum) -> {
                    GitHubLink result = new GitHubLink();
                    result.id(rs.getLong("id"));
                    result.url(rs.getString("url"));
                    result.lastIssueId(rs.getLong("issue_last_id"));
                    result.lastIssueTitle(rs.getString("issue_title"));
                    result.issueCreatorUsername(rs.getString("issue_creator_username"));
                    result.issueCreatedAt(rs.getObject("issue_created_at", OffsetDateTime.class)
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .truncatedTo(ChronoUnit.SECONDS));
                    result.issuePreviewDescription(rs.getString("issue_preview_description"));
                    result.lastPullRequestId(rs.getLong("pull_last_id"));
                    result.lastPullRequestTitle(rs.getString("pull_title"));
                    result.pullCreatorUsername(rs.getString("pull_creator_username"));
                    result.pullCreatedAt(rs.getObject("pull_created_at", OffsetDateTime.class)
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .truncatedTo(ChronoUnit.SECONDS));
                    result.pullPreviewDescription(rs.getString("pull_preview_description"));
                    return result;
                },
                1L);

        assertThat(retrievedLink).isNotNull();
        assertThat(retrievedLink.id()).isEqualTo(link.id());
        assertThat(retrievedLink.lastIssueId()).isEqualTo(link.lastIssueId());
        assertThat(retrievedLink.lastIssueTitle()).isEqualTo(link.lastIssueTitle());
        assertThat(retrievedLink.issueCreatorUsername()).isEqualTo(link.issueCreatorUsername());
        assertThat(retrievedLink.issueCreatedAt()).isEqualTo(link.issueCreatedAt());
        assertThat(retrievedLink.issuePreviewDescription()).isEqualTo(link.issuePreviewDescription());
        assertThat(retrievedLink.lastPullRequestId()).isEqualTo(link.lastPullRequestId());
        assertThat(retrievedLink.lastPullRequestTitle()).isEqualTo(link.lastPullRequestTitle());
        assertThat(retrievedLink.pullCreatorUsername()).isEqualTo(link.pullCreatorUsername());
        assertThat(retrievedLink.pullCreatedAt()).isEqualTo(link.pullCreatedAt());
        assertThat(retrievedLink.pullPreviewDescription()).isEqualTo(link.pullPreviewDescription());
    }

    @Test
    @DisplayName("Обновление GitHubLink: должен обновлять ссылку в базе данных")
    void update_shouldUpdateGithubLink() {
        GitHubLink link = new GitHubLink();
        link.id(2L);
        link.url("http://github.com/test");
        link.lastIssueId(10L);
        link.lastIssueTitle("Test Issue");
        link.issueCreatorUsername("testUser");
        link.issueCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.issuePreviewDescription("Test issue description");
        link.lastPullRequestId(20L);
        link.lastPullRequestTitle("Test Pull Request");
        link.pullCreatorUsername("testUser");
        link.pullCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.pullPreviewDescription("Test pull request description");

        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                2L,
                "http://github.com/test",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        githubLinkRepository.save(link);

        link.lastIssueTitle("Updated Issue");
        link.lastPullRequestTitle("Updated Pull Request");

        githubLinkRepository.update(link);

        GitHubLink updatedLink = jdbcTemplate.queryForObject(
                "SELECT l.id, l.url, gl.* FROM links l JOIN github_links gl ON l.id = gl.link_id WHERE l.id = ?",
                (rs, rowNum) -> {
                    GitHubLink result = new GitHubLink();
                    result.id(rs.getLong("id"));
                    result.url(rs.getString("url"));
                    result.lastIssueId(rs.getLong("issue_last_id"));
                    result.lastIssueTitle(rs.getString("issue_title"));
                    result.issueCreatorUsername(rs.getString("issue_creator_username"));
                    result.issueCreatedAt(rs.getObject("issue_created_at", OffsetDateTime.class)
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .truncatedTo(ChronoUnit.SECONDS));
                    result.issuePreviewDescription(rs.getString("issue_preview_description"));
                    result.lastPullRequestId(rs.getLong("pull_last_id"));
                    result.lastPullRequestTitle(rs.getString("pull_title"));
                    result.pullCreatorUsername(rs.getString("pull_creator_username"));
                    result.pullCreatedAt(rs.getObject("pull_created_at", OffsetDateTime.class)
                            .withOffsetSameInstant(ZoneOffset.UTC)
                            .truncatedTo(ChronoUnit.SECONDS));
                    result.pullPreviewDescription(rs.getString("pull_preview_description"));
                    return result;
                },
                2L);

        assertThat(updatedLink).isNotNull();
        assertThat(updatedLink.lastIssueTitle()).isEqualTo("Updated Issue");
        assertThat(updatedLink.lastPullRequestTitle()).isEqualTo("Updated Pull Request");
    }

    @Test
    @DisplayName("Поиск GitHubLink по ID родительской ссылки: должен возвращать ссылку, если она существует")
    void findByParentId_shouldReturnGithubLinkIfExists() {
        GitHubLink link = new GitHubLink();
        link.id(3L);
        link.url("http://github.com/test");
        link.lastIssueId(10L);
        link.lastIssueTitle("Test Issue");
        link.issueCreatorUsername("testUser");
        link.issueCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.issuePreviewDescription("Test issue description");
        link.lastPullRequestId(20L);
        link.lastPullRequestTitle("Test Pull Request");
        link.pullCreatorUsername("testUser");
        link.pullCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.pullPreviewDescription("Test pull request description");

        // Добавляем ссылку в таблицу links
        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                3L,
                "http://github.com/test",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        githubLinkRepository.save(link);

        Optional<GitHubLink> foundLink = githubLinkRepository.findByParentId(3L);

        assertThat(foundLink).isPresent();
        assertThat(foundLink.get().id()).isEqualTo(link.id());
        assertThat(foundLink.get().lastIssueId()).isEqualTo(link.lastIssueId());
        assertThat(foundLink.get().lastIssueTitle()).isEqualTo(link.lastIssueTitle());
        assertThat(foundLink.get().issueCreatorUsername()).isEqualTo(link.issueCreatorUsername());
        assertThat(foundLink.get().issueCreatedAt()).isEqualTo(link.issueCreatedAt());
        assertThat(foundLink.get().issuePreviewDescription()).isEqualTo(link.issuePreviewDescription());
        assertThat(foundLink.get().lastPullRequestId()).isEqualTo(link.lastPullRequestId());
        assertThat(foundLink.get().lastPullRequestTitle()).isEqualTo(link.lastPullRequestTitle());
        assertThat(foundLink.get().pullCreatorUsername()).isEqualTo(link.pullCreatorUsername());
        assertThat(foundLink.get().pullCreatedAt()).isEqualTo(link.pullCreatedAt());
        assertThat(foundLink.get().pullPreviewDescription()).isEqualTo(link.pullPreviewDescription());
    }

    @Test
    @DisplayName("Удаление GitHubLink по ID родительской ссылки: должен удалять ссылку из базы данных")
    void deleteByParentId_shouldDeleteGithubLink() {
        GitHubLink link = new GitHubLink();
        link.id(4L);
        link.url("http://github.com/test");
        link.lastIssueId(10L);
        link.lastIssueTitle("Test Issue");
        link.issueCreatorUsername("testUser");
        link.issueCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.issuePreviewDescription("Test issue description");
        link.lastPullRequestId(20L);
        link.lastPullRequestTitle("Test Pull Request");
        link.pullCreatorUsername("testUser");
        link.pullCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link.pullPreviewDescription("Test pull request description");

        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                4L,
                "http://github.com/test",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        githubLinkRepository.save(link);

        githubLinkRepository.deleteByParentId(4L);

        Optional<GitHubLink> deletedLink = githubLinkRepository.findByParentId(4L);

        assertThat(deletedLink).isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM links WHERE id = ?", 4L))
                .isEmpty();
    }

    @Test
    @DisplayName("Получение всех GitHubLink: должен возвращать список всех ссылок")
    void findAll_shouldReturnAllGithubLinks() {
        GitHubLink link1 = new GitHubLink();
        link1.id(5L);
        link1.url("http://github.com/test1");
        link1.lastIssueId(10L);
        link1.lastIssueTitle("Test Issue 1");
        link1.issueCreatorUsername("testUser1");
        link1.issueCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link1.issuePreviewDescription("Test issue description 1");
        link1.lastPullRequestId(20L);
        link1.lastPullRequestTitle("Test Pull Request 1");
        link1.pullCreatorUsername("testUser1");
        link1.pullCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link1.pullPreviewDescription("Test pull request description 1");

        GitHubLink link2 = new GitHubLink();
        link2.id(6L);
        link2.url("http://github.com/test2");
        link2.lastIssueId(11L);
        link2.lastIssueTitle("Test Issue 2");
        link2.issueCreatorUsername("testUser2");
        link2.issueCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link2.issuePreviewDescription("Test issue description 2");
        link2.lastPullRequestId(21L);
        link2.lastPullRequestTitle("Test Pull Request 2");
        link2.pullCreatorUsername("testUser2");
        link2.pullCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        link2.pullPreviewDescription("Test pull request description 2");

        // Добавляем ссылки в таблицу links
        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                5L,
                "http://github.com/test1",
                OffsetDateTime.now(),
                OffsetDateTime.now());
        jdbcTemplate.update(
                "INSERT INTO links (id, url, last_updated_at, last_checked_at) VALUES (?, ?, ?, ?)",
                6L,
                "http://github.com/test2",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        githubLinkRepository.save(link1);
        githubLinkRepository.save(link2);

        List<GitHubLink> allLinks = githubLinkRepository.findAll();

        assertThat(allLinks).hasSize(2);
        assertThat(allLinks.get(0).id()).isEqualTo(link1.id());
        assertThat(allLinks.get(1).id()).isEqualTo(link2.id());
    }
}
