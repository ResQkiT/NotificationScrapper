package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.jpa.JpaUserRepository;
import backend.academy.scrapper.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@ConditionalOnProperty(name = "db.access-type", havingValue = "JPA")
public class JpaUserService implements UserService {
    private final JpaUserRepository userRepository;

    @Autowired
    public JpaUserService(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void registerUser(Long id) {
        if (!userRepository.existsById(id)) {
            User user = new User();
            user.id(id);
            userRepository.save(user);
        }
    }

    @Override
    public void removeUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }
}
