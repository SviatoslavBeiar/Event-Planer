package socialMediaApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import socialMediaApp.models.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByUser_Id(int userId);
    void deleteByUser_Id(int userId);
}
