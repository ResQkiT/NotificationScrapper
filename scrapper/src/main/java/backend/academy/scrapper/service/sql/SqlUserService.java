package backend.academy.scrapper.service.sql;

import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.sql.SqlLinkRepository;
import backend.academy.scrapper.repository.sql.SqlUserRepository;
import backend.academy.scrapper.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@ConditionalOnProperty(name = "db.access-type", havingValue = "SQL")
public class SqlUserService implements UserService {

    private final SqlUserRepository userRepository;

    @Autowired
    public SqlUserService(SqlUserRepository userRepository) {
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
