package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JpaFilterRepository extends JpaRepository<Filter, Long> {
    Optional<Filter> findByName(String name);
}
