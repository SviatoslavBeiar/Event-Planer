package socialMediaApp.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import socialMediaApp.api.exp.AlreadyExistsException;
import socialMediaApp.models.OrganizerRequest;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.OrganizerRequestStatus;
import socialMediaApp.models.enums.Role;
import socialMediaApp.repositories.OrganizerRequestRepository;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.requests.LoginRequest;
import socialMediaApp.requests.RegisterRequest;
import socialMediaApp.security.JwtUtil;
import socialMediaApp.services.EmailVerificationService;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OrganizerRequestRepository organizerRequestRepository;
    private final EmailVerificationService emailVerificationService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            OrganizerRequestRepository organizerRequestRepository,
            EmailVerificationService emailVerificationService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.organizerRequestRepository = organizerRequestRepository;
        this.emailVerificationService = emailVerificationService;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {

        String email = normalizeEmail(registerRequest.getEmail());
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("EMAIL_REQUIRED");
        }

        if (userRepository.findByEmail(email) != null) {
            throw new AlreadyExistsException("EMAIL_ALREADY_EXISTS");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(registerRequest.getName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        user.setRole(Role.USER);
        user.setEnabled(false);

        userRepository.save(user);

        if (registerRequest.isOrganizerRequest()
                && !organizerRequestRepository.existsByUser_Id(user.getId())) {

            OrganizerRequest r = new OrganizerRequest();
            r.setUser(user);
            r.setStatus(OrganizerRequestStatus.PENDING);
            organizerRequestRepository.save(r);
        }

        emailVerificationService.createAndSend(user);


        return ResponseEntity.status(HttpStatus.CREATED).body("CHECK_EMAIL");
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String email = normalizeEmail(loginRequest.getEmail());

        User u = userRepository.findByEmail(email);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID_CREDENTIALS");
        }

        if (!Boolean.TRUE.equals(u.isEnabled())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("EMAIL_NOT_VERIFIED");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID_CREDENTIALS");
        }

        String token = jwtUtil.generateToken(
                u.getEmail(),
                u.getId(),
                u.getName() + " " + u.getLastName(),
                u.getRole().name()
        );

        return ResponseEntity.ok(token);
    }



    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        emailVerificationService.verify(token);
        return ResponseEntity.ok("EMAIL_VERIFIED");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resend(@RequestParam String email) {
        emailVerificationService.resend(normalizeEmail(email));
        return ResponseEntity.ok("SENT");
    }

}
