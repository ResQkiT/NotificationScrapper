package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.User;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class UserRepository {

    private Set<User> userSet = new HashSet<>();

    public void addUser(User user) {
        if (user != null) {
            userSet.add(user);
        }
    }

    public boolean removeUser(User user) {
        if (user != null && userSet.contains(user)) {
            userSet.remove(user);
            return true;
        }
        return false;
    }

    public Optional<User> findUserById(Long userId) {
        return userSet.stream().filter(user -> user.id().equals(userId)).findFirst();
    }

    public Set<User> getAllUsers() {
        return new HashSet<>(userSet);
    }

    public boolean userExists(Long userId) {
        return userSet.stream().anyMatch(user -> user.id().equals(userId));
    }

    public int getUserCount() {
        return userSet.size();
    }
}
