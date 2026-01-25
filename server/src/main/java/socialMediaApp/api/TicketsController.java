package socialMediaApp.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import socialMediaApp.requests.TicketVerifyRequest;
import socialMediaApp.responses.ticket.TicketVerifyResponse;
import socialMediaApp.responses.ticket.TicketResponse;

import socialMediaApp.services.CurrentUserService;
import socialMediaApp.services.TicketService;
import socialMediaApp.services.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketsController {

    private final TicketService ticketService;
    private final CurrentUserService current;
    @GetMapping("/availability/{postId}")
    public ResponseEntity<Map<String, Object>> availability(@PathVariable int postId) {
        return ResponseEntity.ok(ticketService.availability(postId));
    }

    @PostMapping("/register/{postId}")
    public ResponseEntity<TicketResponse> register(@PathVariable int postId, Authentication auth) {
        return ResponseEntity.ok(ticketService.register(postId, current.requireUserId(auth)));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<TicketResponse>> mine(Authentication auth) {
        return ResponseEntity.ok(ticketService.getMine(current.requireUserId(auth)));
    }

    @GetMapping("/my/{postId}")
    public ResponseEntity<TicketResponse> getMy(@PathVariable int postId, Authentication auth) {
        return ResponseEntity.ok(ticketService.getMy(postId, current.requireUserId(auth)));
    }

    @PostMapping("/verify/validate")
    public ResponseEntity<TicketVerifyResponse> verifyValidate(@RequestBody TicketVerifyRequest req,
                                                               Authentication auth) {
        var out = ticketService.validate(req.getPostId(), req.getCode(), current.requireUserId(auth));
        return ResponseEntity.ok(out);
    }

    @PostMapping("/verify/consume")
    public ResponseEntity<TicketVerifyResponse> verifyConsume(@RequestBody TicketVerifyRequest req,
                                                              Authentication auth) {
        var out = ticketService.consume(req.getPostId(), req.getCode(), current.requireUserId(auth));
        return ResponseEntity.ok(out);
    }

    @PostMapping("/verify/{postId}")
    public ResponseEntity<TicketResponse> verify(@PathVariable int postId,
                                                 @RequestBody Map<String,String> body,
                                                 Authentication auth) {
        String code = body.getOrDefault("code", "");
        return ResponseEntity.ok(ticketService.verifyAndUse(postId, code, current.requireUserId(auth)));
    }

    @PostMapping("/email/send/{postId}")
    public ResponseEntity<Void> sendMyTicketEmail(@PathVariable int postId, Authentication auth) {
        ticketService.sendMyTicketEmail(postId, current.requireUserId(auth));
        return ResponseEntity.accepted().build();
    }
}
