package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.Role;
import socialMediaApp.services.UserService;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserService userService;

    public User requireUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userService.getByEmailEntity(auth.getName());
    }

    public int requireUserId(Authentication auth) {
        return requireUser(auth).getId();
    }

    public void requireOrganizer(Authentication auth) {
        var me = requireUser(auth);
        if (me.getRole() != Role.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
