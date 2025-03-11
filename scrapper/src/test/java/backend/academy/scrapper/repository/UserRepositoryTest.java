package backend.academy.scrapper.repository;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.entity.User;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository = new UserRepository();
    }

    @Test
    @DisplayName("Добавление пользователя: при валидном пользователе пользователь добавляется в репозиторий")
    public void testAddUser_whenValidUser_thenUserIsAdded() {
        User user = new User(1L);
        userRepository.addUser(user);

        assertTrue(userRepository.userExists(1L));
        assertEquals(1, userRepository.getUserCount());
    }

    @Test
    @DisplayName("Добавление пользователя: при null пользователе ничего не происходит")
    public void testAddUser_whenNullUser_thenDoNothing() {
        userRepository.addUser(null);

        assertEquals(0, userRepository.getUserCount());
    }

    @Test
    @DisplayName("Удаление пользователя: если пользователь существует, он удаляется и возвращается true")
    public void testRemoveUser_whenUserExists_thenUserIsRemoved() {
        User user = new User(2L);
        userRepository.addUser(user);

        boolean result = userRepository.removeUser(user);

        assertTrue(result);
        assertFalse(userRepository.userExists(2L));
        assertEquals(0, userRepository.getUserCount());
    }

    @Test
    @DisplayName("Удаление пользователя: если пользователь не существует, возвращается false")
    public void testRemoveUser_whenUserDoesNotExist_thenReturnFalse() {
        User user = new User(3L);

        boolean result = userRepository.removeUser(user);

        assertFalse(result);
        assertEquals(0, userRepository.getUserCount());
    }

    @Test
    @DisplayName("Поиск пользователя по ID: если пользователь существует, он возвращается")
    public void testFindUserById_whenUserExists_thenReturnUser() {
        User user = new User(4L);
        userRepository.addUser(user);

        Optional<User> foundUser = userRepository.findUserById(4L);

        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
    }

    @Test
    @DisplayName("Поиск пользователя по ID: если пользователь не существует, возвращается пустой Optional")
    public void testFindUserById_whenUserDoesNotExist_thenReturnEmpty() {
        Optional<User> foundUser = userRepository.findUserById(5L);

        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Получение всех пользователей: если пользователи существуют, возвращается их список")
    public void testGetAllUsers_whenUsersExist_thenReturnAllUsers() {
        User user1 = new User(6L);
        User user2 = new User(7L);

        userRepository.addUser(user1);
        userRepository.addUser(user2);

        Set<User> allUsers = userRepository.getAllUsers();

        assertEquals(2, allUsers.size());
        assertTrue(allUsers.contains(user1));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    @DisplayName("Получение всех пользователей: если пользователей нет, возвращается пустой список")
    public void testGetAllUsers_whenNoUsers_thenReturnEmptySet() {
        Set<User> allUsers = userRepository.getAllUsers();

        assertTrue(allUsers.isEmpty());
    }

    @Test
    @DisplayName("Проверка существования пользователя: если пользователь существует, возвращается true")
    public void testUserExists_whenUserExists_thenReturnTrue() {
        User user = new User(8L);
        userRepository.addUser(user);

        assertTrue(userRepository.userExists(8L));
    }

    @Test
    @DisplayName("Проверка существования пользователя: если пользователь не существует, возвращается false")
    public void testUserExists_whenUserDoesNotExist_thenReturnFalse() {
        assertFalse(userRepository.userExists(9L));
    }

    @Test
    @DisplayName("Получение количества пользователей: при добавлении пользователей возвращается корректное количество")
    public void testGetUserCount_whenUsersAdded_thenReturnCorrectCount() {
        userRepository.addUser(new User(10L));
        userRepository.addUser(new User(11L));

        assertEquals(2, userRepository.getUserCount());
    }

    @Test
    @DisplayName("Получение количества пользователей: если пользователей нет, возвращается 0")
    public void testGetUserCount_whenNoUsers_thenReturnZero() {
        assertEquals(0, userRepository.getUserCount());
    }
}
