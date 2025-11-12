package socialMediaApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import socialMediaApp.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    void deleteById(int id);

    User findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}