package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.StackOverflowLink;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SqlStackoverflowLinkRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<StackOverflowLink> rowMapper = new StackoverflowLinkRowMapper();

    public SqlStackoverflowLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Link save(StackOverflowLink link) {
        jdbcTemplate.update(
                "INSERT INTO stackoverflow_links (" + "link_id, answer_last_id, answer_last_username, "
                        + "answer_created_at, answer_preview_description, "
                        + "comment_id, comment_last_username, "
                        + "comment_created_at, comment_preview_description"
                        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                link.id(),
                link.answerLastId(),
                link.answerLastUsername(),
                link.answerCreatedAt(),
                link.answerPreviewDescription(),
                link.commentId(),
                link.commentLastUsername(),
                link.commentCreatedAt(),
                link.commentPreviewDescription());
        return link;
    }

    public StackOverflowLink update(StackOverflowLink link) {
        jdbcTemplate.update(
                "UPDATE stackoverflow_links SET " + "answer_last_id = ?, answer_last_username = ?, "
                        + "answer_created_at = ?, answer_preview_description = ?, "
                        + "comment_id = ?, comment_last_username = ?, "
                        + "comment_created_at = ?, comment_preview_description = ? "
                        + "WHERE link_id = ?",
                link.answerLastId(),
                link.answerLastUsername(),
                link.answerCreatedAt(),
                link.answerPreviewDescription(),
                link.commentId(),
                link.commentLastUsername(),
                link.commentCreatedAt(),
                link.commentPreviewDescription(),
                link.id());
        return link;
    }

    public Optional<StackOverflowLink> findByParentId(Long id) {
        List<StackOverflowLink> result = jdbcTemplate.query(
                "SELECT l.id, l.url, sl.* FROM links l " + "JOIN stackoverflow_links sl ON l.id = sl.link_id "
                        + "WHERE l.id = ?",
                rowMapper,
                id);
        return result.stream().findFirst();
    }

    public void deleteByParentId(Long id) {
        jdbcTemplate.update("DELETE FROM stackoverflow_links WHERE link_id = ?", id);
        jdbcTemplate.update("DELETE FROM links WHERE id = ?", id);
    }

    public List<StackOverflowLink> findAll() {
        return jdbcTemplate.query(
                "SELECT l.id, l.url, sl.* FROM links l " + "JOIN stackoverflow_links sl ON l.id = sl.link_id",
                rowMapper);
    }
    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stackoverflow_links", Long.class);
    }

    private static class StackoverflowLinkRowMapper implements RowMapper<StackOverflowLink> {
        @Override
        public StackOverflowLink mapRow(ResultSet rs, int rowNum) throws SQLException {
            StackOverflowLink link = new StackOverflowLink();

            link.id(rs.getLong("id"));
            link.url(rs.getString("url"));

            link.answerLastId(rs.getLong("answer_last_id"));
            link.answerLastUsername(rs.getString("answer_last_username"));
            link.answerCreatedAt(rs.getObject("answer_created_at", OffsetDateTime.class));
            link.answerPreviewDescription(rs.getString("answer_preview_description"));

            link.commentId(rs.getLong("comment_id"));
            link.commentLastUsername(rs.getString("comment_last_username"));
            link.commentCreatedAt(rs.getObject("comment_created_at", OffsetDateTime.class));
            link.commentPreviewDescription(rs.getString("comment_preview_description"));

            return link;
        }
    }
}
