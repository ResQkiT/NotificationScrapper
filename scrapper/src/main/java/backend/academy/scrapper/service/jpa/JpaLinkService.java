package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.UserRepository;
import backend.academy.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.scrapper.repository.jpa.JpaUserRepository;
import backend.academy.scrapper.service.LinkService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@ConditionalOnProperty(name = "db.access-type", havingValue = "JPA")
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaUserRepository userRepository;

    @Autowired
    public JpaLinkService(JpaLinkRepository linkRepository, JpaUserRepository userRepository) {
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean hasLink(Long userId, String url) {
        return linkRepository.existsByUrlAndUserId(url, userId);
    }

    @Override
    public List<Link> getAllLinks(Long userId) {
        return linkRepository.findAllByUserId(userId);
    }

    @Override
    public Link addLink(Long userId, AddLinkRequest linkRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Link link = new Link(linkRequest.link());
        link.users().add(user);

        return linkRepository.save(link);
    }

    @Override
    public Link updateLink(Link link) {
        return linkRepository.save(link);
    }

    @Override
    public Link removeLink(Long userId, String url) {
        Link link = linkRepository.findByUrlAndUserId(url, userId)
            .orElseThrow(() -> new EntityNotFoundException("Link not found"));
        linkRepository.delete(link);
        return link;
    }
}
