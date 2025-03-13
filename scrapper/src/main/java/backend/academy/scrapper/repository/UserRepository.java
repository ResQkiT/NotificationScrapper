package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void addUser(User user);
    boolean removeUser(User user);
    Optional<User> findUserById(Long userId);
    List<User> getAllUsers();
    boolean userExists(Long userId);
}
