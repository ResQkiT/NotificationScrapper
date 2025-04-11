package backend.academy.scrapper.service;

import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean userExists(Long id) {
        return userRepository.userExists(id);
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public void removeUser(Long id) {
        if (userRepository.userExists(id)) {
            userRepository.removeUserById(id);
        }
    }

    public void registerUser(Long id) {
        userRepository.addUser(new User(id, OffsetDateTime.now()));
    }
}
