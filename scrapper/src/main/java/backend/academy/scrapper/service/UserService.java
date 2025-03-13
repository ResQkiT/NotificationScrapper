package backend.academy.scrapper.service;

import backend.academy.scrapper.model.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    void registerUser(Long id);

    void removeUser(Long id);

    List<User> getAllUsers();

     boolean userExists(Long id);
}
