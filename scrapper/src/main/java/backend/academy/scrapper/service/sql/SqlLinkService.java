package backend.academy.scrapper.service.sql;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.UserRepository;
import backend.academy.scrapper.repository.sql.SqlLinkRepository;
import backend.academy.scrapper.repository.sql.SqlUserRepository;
import backend.academy.scrapper.service.LinkService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@ConditionalOnProperty(name = "db.access-type", havingValue = "SQL")
public class SqlLinkService implements LinkService {

    private final SqlUserRepository userRepository;
    private final SqlLinkRepository linkRepository;

    @Autowired
    public SqlLinkService(SqlUserRepository userRepository, SqlLinkRepository linkRepository) {
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    public boolean hasLink(Long id, String linkUrl) {
        return false;
    }

    @Override
    public List<Link> getAllLinks(Long id) {
        return List.of();
    }

    @Override
    public Link addLink(Long id, AddLinkRequest linkRequest) {
        return null;
    }

    @Override
    public Link updateLink(Link link) {
        return null;
    }

    @Override
    public Link removeLink(Long chatId, String url) {
        return null;
    }
}
