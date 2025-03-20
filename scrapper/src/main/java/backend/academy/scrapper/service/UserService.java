package backend.academy.scrapper.service;

import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.UserRepository;
import backend.academy.scrapper.repository.sql.SqlUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean userExists(Long id) {
        return userRepository.userExists(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public void removeUser(Long id) {
        if(userRepository.userExists(id)) {
            userRepository.removeUserById(id);
        }
    }

    @Override
    public void registerUser(Long id) {
        userRepository.addUser(new User(id, LocalDateTime.now()));
    }
}
