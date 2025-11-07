package socialMediaApp.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.Role;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.requests.LoginRequest;
import socialMediaApp.requests.RegisterRequest;
import socialMediaApp.security.JwtUtil;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,  JwtUtil jwtUtil, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest){
        if (userRepository.findByEmail(registerRequest.getEmail())!=null){
            return new ResponseEntity<>("Email already exist",HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setName(registerRequest.getName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER);

        userRepository.save(user);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        );

        String token = jwtUtil.generateToken(
                registerRequest.getEmail(),
                user.getId(),
                user.getName() + " " + user.getLastName(),
                user.getRole().name()
        );
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest)  {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            User u = userRepository.findByEmail(loginRequest.getEmail());
            String token = jwtUtil.generateToken(
                    u.getEmail(),
                    u.getId(),
                    u.getName() + " " + u.getLastName(),
                    u.getRole().name()
            );
            return new ResponseEntity<>(token, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }


}
