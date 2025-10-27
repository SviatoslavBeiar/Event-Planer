// src/main/java/socialMediaApp/api/TicketsController.java
package socialMediaApp.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import socialMediaApp.repositories.TicketRepository;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.requests.TicketVerifyRequest;
import socialMediaApp.responses.TicketVerifyResponse;
import socialMediaApp.responses.ticket.TicketResponse;
import socialMediaApp.services.PostService;
import socialMediaApp.services.TicketService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketsController {

    private final TicketService ticketService;
    private final UserRepository userRepository;
    private final PostService postService;
    private final TicketRepository ticketRepository;

    // src/main/java/socialMediaApp/api/TicketsController.java
    @GetMapping("/availability/{postId}")
    public ResponseEntity<Map<String, Object>> availability(@PathVariable int postId) {
        var post = postService.getById(postId);
        long sold = ticketRepository.countByPost_Id(postId);
        Integer capacity = post.getCapacity();
        boolean full = capacity != null && sold >= capacity;
        return ResponseEntity.ok(Map.of(
                "sold", sold,
                "capacity", capacity,
                "remaining", capacity == null ? null : Math.max(0, capacity - sold),
                "full", full
        ));
    }

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

    @PostMapping("/verify/validate")
    public ResponseEntity<TicketVerifyResponse> verifyValidate(@RequestBody TicketVerifyRequest req, Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var out = ticketService.validate(req.getPostId(), req.getCode(), me.getId());
        // 200 завжди з valid=true/false
        return ResponseEntity.ok(out);
    }

    @PostMapping("/verify/consume")
    public ResponseEntity<TicketVerifyResponse> verifyConsume(@RequestBody TicketVerifyRequest req, Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var out = ticketService.consume(req.getPostId(), req.getCode(), me.getId());
        return ResponseEntity.ok(out);
    }


    // src/main/java/socialMediaApp/api/TicketsController.java
    @PostMapping("/verify/{postId}")
    public ResponseEntity<TicketResponse> verify(@PathVariable int postId,
                                                 @RequestBody Map<String,String> body,
                                                 Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String code = body.getOrDefault("code", "");
        return ResponseEntity.ok(ticketService.verifyAndUse(postId, code, me.getId()));
    }

}
