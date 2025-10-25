// src/main/java/socialMediaApp/api/TicketsController.java
package socialMediaApp.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.responses.ticket.TicketResponse;
import socialMediaApp.services.TicketService;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketsController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    // зареєструватися на подію
    @PostMapping("/register/{postId}")
    public ResponseEntity<TicketResponse> register(@PathVariable int postId, Authentication auth) {
        int meId = userRepository.findByEmail(auth.getName()).getId();
        return ResponseEntity.ok(ticketService.register(postId, meId));
    }
    @GetMapping("/mine")
    public ResponseEntity<List<TicketResponse>> mine(Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(ticketService.getMine(me.getId()));
    }
    // мій квиток на цю подію (404 якщо немає)
    @GetMapping("/my/{postId}")
    public ResponseEntity<TicketResponse> getMy(@PathVariable int postId, Authentication auth) {
        int meId = userRepository.findByEmail(auth.getName()).getId();
        return ResponseEntity.ok(ticketService.getMy(postId, meId));
    }
}
