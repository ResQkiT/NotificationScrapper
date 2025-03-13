package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@ConditionalOnProperty(name = "db.access-type", havingValue = "SQL")
public class SqlLinkRepository implements LinkRepository {
    @Override
    public Link addLink(Long userId, AddLinkRequest linkRequest) {
        return null;
    }

    @Override
    public boolean hasLink(Long userId, AddLinkRequest linkRequest) {
        return false;
    }

    @Override
    public boolean removeLink(Long userId, String url) {
        return false;
    }

    @Override
    public List<Link> getLinks(Long userId) {
        return List.of();
    }

    @Override
    public Link updateLink(Link link) {
        return null;
    }
}
