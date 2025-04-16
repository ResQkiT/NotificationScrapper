package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.BaseRepositoryTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@Transactional
@Import(JpaUserRepository.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "db.access-type=JPA")
public class JpaUserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private JpaUserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("Добавление пользователя: пользователь должен быть добавлен в базу данных")
    void addUser_shouldAddUserToDatabase() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        User user = new User(1L, now);
        userRepository.addUser(user);

        entityManager.flush();

        User retrievedUser = jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE id = ?",
                (rs, rowNum) -> new User(rs.getLong("id"), rs.getObject("created_at", OffsetDateTime.class)),
                user.id());

        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.id()).isEqualTo(user.id());
        assertThat(retrievedUser
                        .createdAt()
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(now);
    }

    @Test
    @DisplayName("Удаление пользователя по ID: пользователь должен быть удален из базы данных")
    void removeUserById_shouldRemoveUserFromDatabase() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        User user = new User(2L, now);
        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?)", user.id(), user.createdAt());

        entityManager.flush();

        boolean removed = userRepository.removeUserById(user.id());

        entityManager.flush();

        assertThat(removed).isTrue();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM users WHERE id = ?", user.id()))
                .isEmpty();
    }

    @Test
    @DisplayName("Поиск пользователя по ID: должен вернуть пользователя, если он существует")
    void findUserById_shouldReturnUserIfExists() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        User user = new User(3L, now);
        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?)", user.id(), user.createdAt());

        User foundUser = userRepository.findUserById(user.id()).orElse(null);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.id()).isEqualTo(user.id());
        assertThat(foundUser.createdAt().withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(user.createdAt());
    }

    @Test
    @DisplayName("Получение всех пользователей: должен вернуть список всех пользователей")
    void getAllUsers_shouldReturnAllUsers() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        User user1 = new User(4L, now);
        User user2 = new User(5L, now);
        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?)", user1.id(), user1.createdAt());
        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?)", user2.id(), user2.createdAt());

        List<User> users = userRepository.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users.stream().map(User::id)).containsExactlyInAnyOrder(user1.id(), user2.id());
        assertThat(users.stream().map(User::createdAt).map(odt -> odt.withOffsetSameInstant(ZoneOffset.UTC)
                        .truncatedTo(ChronoUnit.SECONDS)))
                .containsExactlyInAnyOrder(user1.createdAt(), user2.createdAt());
    }

    @Test
    @DisplayName("Проверка существования пользователя: должен вернуть true, если пользователь существует")
    void userExists_shouldReturnTrueIfUserExists() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        User user = new User(6L, now);
        jdbcTemplate.update("INSERT INTO users (id, created_at) VALUES (?, ?)", user.id(), user.createdAt());

        boolean exists = userRepository.userExists(user.id());

        assertThat(exists).isTrue();
    }
}
