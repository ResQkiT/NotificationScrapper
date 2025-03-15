package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JpaTagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}
