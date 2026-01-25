package socialMediaApp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.Role;
import socialMediaApp.repositories.UserRepository;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@local}")
    private String adminEmail;

    @Value("${app.admin.password:admin12345}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) return;

        User u = new User();
        u.setName("Admin");
        u.setLastName("System");
        u.setEmail(adminEmail);
        u.setPassword(passwordEncoder.encode(adminPassword));
        u.setRole(Role.ADMIN);
        u.setEnabled(true);

        userRepository.save(u);
    }
}
