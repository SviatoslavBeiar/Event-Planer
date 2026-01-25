package socialMediaApp.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import socialMediaApp.responses.event.EventCheckerResponse;

import socialMediaApp.services.CurrentUserService;
import socialMediaApp.services.EventCheckerService;

import java.util.List;

@RestController
@RequestMapping("/api/event-checkers")
@RequiredArgsConstructor
public class EventCheckersController {

    private final EventCheckerService service;
    private final CurrentUserService current;

    @GetMapping("/by-post/{postId}")
    public ResponseEntity<List<EventCheckerResponse>> byPost(@PathVariable int postId,
                                                             Authentication auth) {
        int meId = current.requireUserId(auth);
        return ResponseEntity.ok(service.byPost(postId, meId));
    }

    @GetMapping("/mine")
    public List<EventCheckerResponse> mine(Authentication auth) {
        return service.mine(current.requireUserId(auth));
    }

    @GetMapping("/am-i-checker/{postId}")
    public boolean amIChecker(@PathVariable int postId, Authentication auth) {
        return service.amIChecker(postId, current.requireUserId(auth));
    }

    @PostMapping("/assign/{postId}/{userId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventCheckerResponse assign(@PathVariable int postId,
                                       @PathVariable int userId,
                                       Authentication auth) {
        return service.assign(postId, current.requireUserId(auth), userId);
    }

    @DeleteMapping("/revoke/{postId}/{userId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public void revoke(@PathVariable int postId,
                       @PathVariable int userId,
                       Authentication auth) {
        service.revoke(postId, current.requireUserId(auth), userId);
    }

    @PostMapping("/assign-by-email/{postId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventCheckerResponse assignByEmail(@PathVariable int postId,
                                              @RequestParam String email,
                                              Authentication auth) {
        return service.assignByEmail(postId, current.requireUserId(auth), email);
    }

    @DeleteMapping("/revoke-by-email/{postId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public void revokeByEmail(@PathVariable int postId,
                              @RequestParam String email,
                              Authentication auth) {
        service.revokeByEmail(postId, current.requireUserId(auth), email);
    }
}
