package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
@ConditionalOnProperty(name = "db.access-type", havingValue = "SQL")
public class SqlUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public SqlUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<User> USER_ROW_MAPPER =
            (rs, rowNum) -> new User(rs.getLong("id"), rs.getObject("created_at", OffsetDateTime.class));

    @Override
    public void addUser(User user) {
        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?) ON CONFLICT DO NOTHING", ps -> {
            ps.setObject(1, user.id());
            ps.setObject(2, user.createdAt(), Types.TIMESTAMP_WITH_TIMEZONE);
        });
    }

    @Override
    public boolean removeUserById(Long id) {
        int rows = jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        return rows > 0;
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = ?", USER_ROW_MAPPER, userId).stream()
                .findFirst();
    }

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM users", USER_ROW_MAPPER);
    }

    @Override
    public boolean userExists(Long userId) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)", Boolean.class, userId));
    }
}
