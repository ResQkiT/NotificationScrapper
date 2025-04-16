package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Tag;
import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.LinkRepository;
import jakarta.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
@RequiredArgsConstructor
@ConditionalOnProperty(name = "db.access-type", havingValue = "SQL")
public class SqlLinkRepository implements LinkRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Link> LINK_ROW_MAPPER = (rs, rowNum) -> new Link(
            rs.getLong("id"),
            rs.getString("url"),
            rs.getObject("last_updated_at", OffsetDateTime.class),
            rs.getObject("last_checked_at", OffsetDateTime.class));

    @Override
    public Link addLink(Long userId, AddLinkRequest linkRequest) {
        String findLinkSql = "SELECT id FROM links WHERE url = ?";
        Optional<Long> existingLinkId = jdbcTemplate.queryForList(findLinkSql, Long.class, linkRequest.link()).stream()
                .findFirst();

        OffsetDateTime now = OffsetDateTime.now();

        if (existingLinkId.isPresent()) {
            String addSubscriptionSql = "INSERT INTO subscriptions (link_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(addSubscriptionSql, existingLinkId.get(), userId);

            Long linkId = existingLinkId.get();
            List<User> subscribedUsers = getSubscribedUsers(linkId);
            List<Tag> tags = getTagsForLink(linkId);
            List<Filter> filters = getFiltersForLink(linkId);

            return new Link(linkId, linkRequest.link(), now, now, subscribedUsers, tags, filters);
        } else {
            String insertLinkSql =
                    """
            INSERT INTO links (url, last_updated_at, last_checked_at)
            VALUES (?, ?, ?)
            RETURNING id
            """;

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps =
                                connection.prepareStatement(insertLinkSql, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, linkRequest.link());
                        ps.setObject(2, now, Types.TIMESTAMP_WITH_TIMEZONE);
                        ps.setNull(3, Types.TIMESTAMP_WITH_TIMEZONE);
                        return ps;
                    },
                    keyHolder);

            Long linkId = Optional.ofNullable(keyHolder.getKey())
                    .map(Number::longValue)
                    .orElseThrow(() -> new RuntimeException("Failed to add link"));

            String addSubscriptionSql = "INSERT INTO subscriptions (link_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(addSubscriptionSql, linkId, userId);

            addTagsToLink(linkId, linkRequest.tags());
            addFiltersToLink(linkId, linkRequest.filters());

            List<User> subscribedUsers = getSubscribedUsers(linkId);
            List<Tag> tags = getTagsForLink(linkId);
            List<Filter> filters = getFiltersForLink(linkId);

            return new Link(linkId, linkRequest.link(), now, null, subscribedUsers, tags, filters);
        }
    }

    private List<User> getSubscribedUsers(Long linkId) {
        String sql =
                """
        SELECT u.id, u.created_at FROM users u
        JOIN subscriptions s ON u.id = s.user_id
        WHERE s.link_id = ?
        """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    User user = new User();
                    user.id(rs.getLong("id"));
                    user.createdAt(rs.getObject("created_at", OffsetDateTime.class));
                    return user;
                },
                linkId);
    }

    private List<Tag> getTagsForLink(Long linkId) {
        String sql =
                """
        SELECT t.id, t.name FROM tags t
        JOIN link_tags lt ON t.id = lt.tag_id
        WHERE lt.link_id = ?
        """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Tag tag = new Tag();
                    tag.id(rs.getLong("id"));
                    tag.name(rs.getString("name"));
                    return tag;
                },
                linkId);
    }

    private List<Filter> getFiltersForLink(Long linkId) {
        String sql =
                """
        SELECT f.id, f.name FROM filters f
        JOIN link_filters lf ON f.id = lf.filter_id
        WHERE lf.link_id = ?
        """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Filter filter = new Filter();
                    filter.id(rs.getLong("id"));
                    filter.name(rs.getString("name"));
                    return filter;
                },
                linkId);
    }

    @Override
    public boolean hasLink(Long userId, String url) {
        String sql =
                """
            SELECT COUNT(*) FROM links l
            JOIN subscriptions s ON l.id = s.link_id
            WHERE s.user_id = ? AND l.url = ?
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, url);
        return count != null && count > 0;
    }

    @Override
    public Link removeLink(Long userId, String url) {
        String findLinkSql = "SELECT id FROM links WHERE url = ?";
        Optional<Long> linkId =
                jdbcTemplate.queryForList(findLinkSql, Long.class, url).stream().findFirst();

        if (linkId.isPresent()) {
            Optional<Link> link = findLinkById(linkId.get());

            String deleteSubscriptionSql = "DELETE FROM subscriptions WHERE link_id = ? AND user_id = ?";
            int deletedRows = jdbcTemplate.update(deleteSubscriptionSql, linkId.get(), userId);

            String countSubscriptionsSql = "SELECT COUNT(*) FROM subscriptions WHERE link_id = ?";
            Integer subscriptionCount = jdbcTemplate.queryForObject(countSubscriptionsSql, Integer.class, linkId.get());

            if (subscriptionCount != null && subscriptionCount == 0) {
                String deleteTagsSql = "DELETE FROM link_tags WHERE link_id = ?";
                String deleteFiltersSql = "DELETE FROM link_filters WHERE link_id = ?";
                jdbcTemplate.update(deleteTagsSql, linkId.get());
                jdbcTemplate.update(deleteFiltersSql, linkId.get());

                String deleteLinkSql = "DELETE FROM links WHERE id = ?";
                jdbcTemplate.update(deleteLinkSql, linkId.get());
            }

            if (deletedRows > 0 && link.isPresent()) {
                return link.get();
            }
        }
        return null;
    }

    @Override
    public List<Link> getLinks(Long userId) {
        String sql =
                """
        SELECT l.id, l.url, l.last_updated_at, l.last_checked_at
        FROM links l
        JOIN subscriptions s ON l.id = s.link_id
        WHERE s.user_id = ?
        """;

        List<Link> links = jdbcTemplate.query(sql, LINK_ROW_MAPPER, userId);

        for (Link link : links) {
            Long linkId = link.id();

            List<User> subscribedUsers = getSubscribedUsers(linkId);
            link.users(subscribedUsers);

            List<Tag> tags = getTagsForLink(linkId);
            link.tags(tags);

            List<Filter> filters = getFiltersForLink(linkId);
            link.filters(filters);
        }

        return links;
    }

    @Override
    public List<Link> getAllLinksWithDelay(Duration delay) {
        OffsetDateTime threshold = OffsetDateTime.now().minus(delay);

        String sql =
                """
        SELECT id, url, last_updated_at, last_checked_at
        FROM links
        WHERE last_checked_at <= ? OR last_checked_at IS NULL
        """;

        List<Link> links = jdbcTemplate.query(sql, LINK_ROW_MAPPER, threshold);

        for (Link link : links) {
            Long linkId = link.id();
            link.users(getSubscribedUsers(linkId));
            link.tags(getTagsForLink(linkId));
            link.filters(getFiltersForLink(linkId));
        }

        return links;
    }

    @Override
    public Link updateLink(Link link) {
        String sql =
                """
            UPDATE links
            SET last_updated_at = ?, last_checked_at = ?
            WHERE id = ?
            """;
        jdbcTemplate.update(sql, link.lastUpdatedAt(), link.lastCheckedAt(), link.id());
        return link;
    }

    @Override
    public Optional<Link> findLinkById(Long linkId) {
        String sql = "SELECT id, url, last_updated_at, last_checked_at FROM links WHERE id = ?";
        try {
            Link link = jdbcTemplate.queryForObject(sql, LINK_ROW_MAPPER, linkId);

            if (link != null) {
                List<User> subscribedUsers = getSubscribedUsers(linkId);
                link.users(subscribedUsers);

                List<Tag> tags = getTagsForLink(linkId);
                link.tags(tags);

                List<Filter> filters = getFiltersForLink(linkId);
                link.filters(filters);
            }

            return Optional.ofNullable(link);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private void addTagsToLink(Long linkId, List<String> tags) {
        if (tags == null || tags.isEmpty()) return;

        String insertTagSql = "INSERT INTO tags (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
        String insertLinkTagSql = "INSERT INTO link_tags (link_id, tag_id) VALUES (?, ?)";

        for (String tagName : tags) {
            jdbcTemplate.update(insertTagSql, tagName);

            String findTagIdSql = "SELECT id FROM tags WHERE name = ?";
            Long tagId = jdbcTemplate.queryForObject(findTagIdSql, Long.class, tagName);

            jdbcTemplate.update(insertLinkTagSql, linkId, tagId);
        }
    }

    private void addFiltersToLink(Long linkId, List<String> filters) {
        if (filters == null || filters.isEmpty()) return;

        String insertFilterSql = "INSERT INTO filters (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
        String insertLinkFilterSql = "INSERT INTO link_filters (link_id, filter_id) VALUES (?, ?)";

        for (String filterName : filters) {
            jdbcTemplate.update(insertFilterSql, filterName);
            String findFilterIdSql = "SELECT id FROM filters WHERE name = ?";
            Long filterId = jdbcTemplate.queryForObject(findFilterIdSql, Long.class, filterName);
            jdbcTemplate.update(insertLinkFilterSql, linkId, filterId);
        }
    }
}
