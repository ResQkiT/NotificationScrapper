package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.Link;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
@Transactional
@ConditionalOnProperty(name = "db.access-type", havingValue = "JPA")
public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    boolean existsByUrlAndUserId(String url, Long userId);


    Optional<Link> findByUrlAndUserId(String url, Long userId);


    List<Link> findAllByUserId(Long userId);
}
