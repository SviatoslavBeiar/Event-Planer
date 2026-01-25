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
public class PerfUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.perf.email:perf_user@test.com}")
    private String email;

    @Value("${app.perf.password:Password123!}")
    private String password;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase(email)) return;

        User u = new User();
        u.setName("Perf");
        u.setLastName("User");
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(Role.ADMIN);
        u.setEnabled(true);

        userRepository.save(u);
    }
}
