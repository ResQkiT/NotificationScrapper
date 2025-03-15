package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.LinkRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
@ConditionalOnProperty(name = "db.access-type", havingValue = "JPA")
public interface JpaUserRepository  extends JpaRepository<User, Long> {

}
