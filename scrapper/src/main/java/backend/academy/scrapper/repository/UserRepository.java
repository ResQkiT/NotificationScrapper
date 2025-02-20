package backend.academy.scrapper.repository;

import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class UserRepository {

    private Set<User> userSet = new HashSet<>();

    public void addUser(User user) {
        if (user != null) {
            userSet.add(user);
            System.out.println("Пользователь " + user.firstName() + " добавлен.");
        } else {
            System.out.println("Не удалось добавить пользователя, так как он равен null.");
        }
    }

    public boolean removeUser(User user) {
        if (user != null && userSet.contains(user)) {
            userSet.remove(user);
            System.out.println("Пользователь " + user.firstName() + " удален.");
            return true;
        }
        System.out.println("Пользователь не найден.");
        return false;
    }

    public Optional<User> findUserById(Long userId) {
        return userSet.stream()
            .filter(user -> user.id().equals(userId))
            .findFirst();
    }

    public Set<User> getAllUsers() {
        return new HashSet<>(userSet);
    }

    public boolean userExists(Long userId) {
        return userSet.stream()
            .anyMatch(user -> user.id().equals(userId));
    }

    public int getUserCount() {
        return userSet.size();
    }
}
