package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.models.EmailVerificationToken;
import socialMediaApp.models.User;
import socialMediaApp.repositories.EmailVerificationTokenRepository;
import socialMediaApp.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final MailService mailService;

    private final String frontendBaseUrl = "http://localhost:3000";

    @Transactional
    public void createAndSend(User user) {
        tokenRepo.deleteByUser_Id(user.getId());

        String token = UUID.randomUUID().toString().replace("-", "");

        EmailVerificationToken t = new EmailVerificationToken();
        t.setUser(user);
        t.setToken(token);
        t.setExpiresAt(LocalDateTime.now().plusHours(24));
        tokenRepo.save(t);

        String link = frontendBaseUrl + "/verify-email?token=" + token;
        mailService.sendAccountVerificationEmail(user.getEmail(), link);
    }

    @Transactional
    public void verify(String token) {
        EmailVerificationToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new NotFoundException("TOKEN_NOT_FOUND"));

        if (t.getUsedAt() != null) throw new IllegalStateException("TOKEN_ALREADY_USED");
        if (t.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("TOKEN_EXPIRED");

        User u = t.getUser();
        u.setEnabled(true);
        userRepo.save(u);

        t.setUsedAt(LocalDateTime.now());
        tokenRepo.save(t);
    }

    @Transactional
    public void resend(String email) {
        User u = userRepo.findByEmail(email);
        if (u == null) throw new NotFoundException("USER_NOT_FOUND");
        if (Boolean.TRUE.equals(u.isEnabled())) return; // already verified
        createAndSend(u);
    }
}
