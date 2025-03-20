package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.User;
import backend.academy.scrapper.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@ConditionalOnProperty(name = "db.access-type", havingValue = "JPA")
public class JpaUserRepository implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addUser(User user) {
        if(userExists(user.id())) return;

        entityManager.persist(user);
    }

    @Override
    public boolean removeUserById(Long id) {
        User managedUser = entityManager.find(User.class, id);
        if (managedUser != null) {
            entityManager.remove(managedUser);
            return true;
        }
        return false;
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        return Optional.ofNullable(entityManager.find(User.class, userId));
    }

    @Override
    public List<User> getAllUsers() {
        return entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    @Override
    public boolean userExists(Long userId) {
        Long count = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.id = :userId", Long.class)
            .setParameter("userId", userId)
            .getSingleResult();
        return count > 0;
    }
}

