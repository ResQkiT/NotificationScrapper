package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.User;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(Long id) {
        if(userRepository.userExists(id)) return;
        userRepository.addUser(new User(id));
    }

    public void removeUser(Long id) {
        var optionalUser = userRepository.findUserById(id);
        optionalUser.ifPresent(userRepository::removeUser);
    }

    public List<User> getAllUsers(){
        return userRepository.getAllUsers().stream().toList();
    }

    public boolean userExists(Long id) {
        return userRepository.userExists(id);
    }
}
