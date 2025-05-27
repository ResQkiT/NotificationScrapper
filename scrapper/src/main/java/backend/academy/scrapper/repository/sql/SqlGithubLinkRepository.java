package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.model.GitHubLink;
import backend.academy.scrapper.model.Link;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SqlGithubLinkRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<GitHubLink> rowMapper = new GithubLinkRowMapper();

    public SqlGithubLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Link save(GitHubLink link) {
        jdbcTemplate.update(
                "INSERT INTO github_links (" + "link_id, issue_last_id, issue_title, issue_creator_username, "
                        + "issue_created_at, issue_preview_description, pull_last_id, "
                        + "pull_title, pull_creator_username, pull_created_at, pull_preview_description"
                        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                link.id(),
                link.lastIssueId(),
                link.lastIssueTitle(),
                link.issueCreatorUsername(),
                link.issueCreatedAt(),
                link.issuePreviewDescription(),
                link.lastPullRequestId(),
                link.lastPullRequestTitle(),
                link.pullCreatorUsername(),
                link.pullCreatedAt(),
                link.pullPreviewDescription());

        return link;
    }

    public GitHubLink update(GitHubLink link) {

        jdbcTemplate.update(
                "UPDATE github_links SET " + "issue_last_id = ?, issue_title = ?, issue_creator_username = ?, "
                        + "issue_created_at = ?, issue_preview_description = ?, pull_last_id = ?, "
                        + "pull_title = ?, pull_creator_username = ?, pull_created_at = ?, "
                        + "pull_preview_description = ? "
                        + "WHERE link_id = ?",
                link.lastIssueId(),
                link.lastIssueTitle(),
                link.issueCreatorUsername(),
                link.issueCreatedAt(),
                link.issuePreviewDescription(),
                link.lastPullRequestId(),
                link.lastPullRequestTitle(),
                link.pullCreatorUsername(),
                link.pullCreatedAt(),
                link.pullPreviewDescription(),
                link.id());

        return link;
    }

    public Optional<GitHubLink> findByParentId(Long id) {
        List<GitHubLink> result = jdbcTemplate.query(
                "SELECT l.id, l.url, gl.* FROM links l " + "JOIN github_links gl ON l.id = gl.link_id "
                        + "WHERE l.id = ?",
                rowMapper,
                id);
        return result.stream().findFirst();
    }

    public void deleteByParentId(Long id) {
        jdbcTemplate.update("DELETE FROM github_links WHERE link_id = ?", id);
        jdbcTemplate.update("DELETE FROM links WHERE id = ?", id);
    }

    public List<GitHubLink> findAll() {
        return jdbcTemplate.query(
                "SELECT l.id, l.url, gl.* FROM links l " + "JOIN github_links gl ON l.id = gl.link_id", rowMapper);
    }

    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM github_links", Long.class);
    }

    private static class GithubLinkRowMapper implements RowMapper<GitHubLink> {
        @Override
        public GitHubLink mapRow(ResultSet rs, int rowNum) throws SQLException {
            GitHubLink link = new GitHubLink();

            link.id(rs.getLong("id"));
            link.url(rs.getString("url"));

            link.lastIssueId(rs.getLong("issue_last_id"));
            link.lastIssueTitle(rs.getString("issue_title"));
            link.issueCreatorUsername(rs.getString("issue_creator_username"));
            link.issueCreatedAt(rs.getObject("issue_created_at", OffsetDateTime.class));
            link.issuePreviewDescription(rs.getString("issue_preview_description"));

            link.lastPullRequestId(rs.getLong("pull_last_id"));
            link.lastPullRequestTitle(rs.getString("pull_title"));
            link.pullCreatorUsername(rs.getString("pull_creator_username"));
            link.pullCreatedAt(rs.getObject("pull_created_at", OffsetDateTime.class));
            link.pullPreviewDescription(rs.getString("pull_preview_description"));

            return link;
        }
    }


}
