// src/main/java/socialMediaApp/api/EventCheckersController.java
package socialMediaApp.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import socialMediaApp.models.enums.Role;
import socialMediaApp.repositories.UserRepository;
import socialMediaApp.responses.EventCheckerResponse;
import socialMediaApp.services.EventCheckerService;

import java.util.List;

@RestController
@RequestMapping("/api/event-checkers")
@RequiredArgsConstructor
public class EventCheckersController {

    private final EventCheckerService service;
    private final UserRepository userRepository;


    // src/main/java/socialMediaApp/api/EventCheckersController.java
    @GetMapping("/by-post/{postId}")
    public ResponseEntity<List<EventCheckerResponse>> byPost(@PathVariable int postId,
                                                             Authentication auth) {
        int meId = userRepository.findByEmail(auth.getName()).getId();
        return ResponseEntity.ok(service.byPost(postId, meId));
    }


    @GetMapping("/mine")
    public List<EventCheckerResponse> mine(Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return service.mine(me.getId());
    }

    @GetMapping("/am-i-checker/{postId}")
    public boolean amIChecker(@PathVariable int postId, Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return service.amIChecker(postId, me.getId());
    }

    @PostMapping("/assign/{postId}/{userId}")
    public EventCheckerResponse assign(@PathVariable int postId,
                                       @PathVariable int userId,
                                       Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (me.getRole() != Role.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return service.assign(postId, me.getId(), userId);
    }

    @DeleteMapping("/revoke/{postId}/{userId}")
    public void revoke(@PathVariable int postId,
                       @PathVariable int userId,
                       Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (me.getRole() != Role.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        service.revoke(postId, me.getId(), userId);
    }


    @PostMapping("/assign-by-email/{postId}")
    public EventCheckerResponse assignByEmail(@PathVariable int postId,
                                              @RequestParam String email,
                                              Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return service.assignByEmail(postId, me.getId(), email);
    }

    @DeleteMapping("/revoke-by-email/{postId}")
    public void revokeByEmail(@PathVariable int postId,
                              @RequestParam String email,
                              Authentication auth) {
        var me = userRepository.findByEmail(auth.getName());
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        service.revokeByEmail(postId, me.getId(), email);
    }
}
