package backend.academy.scrapper.service;

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

    private final LinkRepository linkRepository;

    @Autowired
    public UserService(UserRepository userRepository, LinkRepository linkRepository) {
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
    }


    public void registerUser(Long id) {
        if(userRepository.userExists(id)) return;

        userRepository.addUser(new User(id));
    }

    public void removeUser(Long id) {
        var optionalUser = userRepository.findUserById(id);
        optionalUser.ifPresent(userRepository::removeUser);
    }

    public List<Link> getAllLinks(Long id){
        return linkRepository.getLinks(id);
    }

    public Link addLink(Long id, Link link){
        linkRepository.addLink(id, link);
        return link;
    }

    public Link removeLink(Long chatId, String url) {
        var linkToRemove = linkRepository.getLinks(chatId).stream()
            .filter(l -> l.url().equals(url))
            .findFirst();

        linkToRemove.ifPresent(l -> linkRepository.removeLink(chatId, l.url()));
        return linkToRemove.orElse(null);
    }

    public boolean userExists(Long id) {
        return userRepository.userExists(id);
    }
}
