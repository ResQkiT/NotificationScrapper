package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.Link;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
@Transactional
@ConditionalOnProperty(name = "db.access-type", havingValue = "JPA")
public interface JpaLinkRepository extends JpaRepository<Link, Long> {
    boolean existsByUrlAndUsersId(String url, Long userId);
    Optional<Link> findByUrl(String url);

    List<Link> findAllByUsersId(Long id);

    @EntityGraph(attributePaths = {"tags", "filters", "users"})
    Optional<Link> findWithDetailsById(Long id);

    boolean existsByUrl(String url);
}
