package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import socialMediaApp.api.exp.AlreadyExistsException;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.EventCheckerMapper;
import socialMediaApp.models.EventChecker;
import socialMediaApp.models.Post;
import socialMediaApp.models.User;
import socialMediaApp.repositories.EventCheckerRepository;
import socialMediaApp.responses.event.EventCheckerResponse;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventCheckerService {

    private static final String ONLY_OWNER = "ONLY_EVENT_OWNER";
    private static final String ONLY_OWNER_SHORT = "ONLY_OWNER";
    private static final String ALREADY_CHECKER = "Already a checker";
    private static final String CHECKER_NOT_FOUND = "Checker not found";
    private static final String EMAIL_EMPTY = "EMAIL_EMPTY";

    private final EventCheckerRepository repo;
    private final EventCheckerMapper mapper;
    private final PostService postService;
    private final UserService userService;

    // ---------- Queries ----------

    public List<EventCheckerResponse> byPost(int postId, int requesterId) {
        Post post = postService.getById(postId);
        assertOwner(post.getUser().getId(), requesterId, ONLY_OWNER_SHORT);
        return mapper.toResponses(repo.findAllByPost_IdOrderByCreatedAtDesc(postId));
    }

    public List<EventCheckerResponse> mine(int userId) {
        return mapper.toResponses(repo.findAllByUser_IdOrderByCreatedAtDesc(userId));
    }

    public boolean amIChecker(int postId, int userId) {
        return repo.existsByPost_IdAndUser_Id(postId, userId);
    }

    // ---------- Commands ----------

    @Transactional
    public EventCheckerResponse assign(int postId, int organizerId, int userId) {
        Post post = postService.getById(postId);
        assertOwner(post.getUser().getId(), organizerId, ONLY_OWNER);

        ensureNotAlreadyChecker(postId, userId);

        User u = userService.getById(userId);
        EventChecker ec = new EventChecker();
        ec.setPost(post);
        ec.setUser(u);
        repo.save(ec);
        return mapper.toResponse(ec);
    }

    @Transactional
    public void revoke(int postId, int organizerId, int userId) {
        Post post = postService.getById(postId);
        assertOwner(post.getUser().getId(), organizerId, ONLY_OWNER);

        if (!repo.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new NotFoundException(CHECKER_NOT_FOUND);
        }

        repo.deleteByPost_IdAndUser_Id(postId, userId);
    }

    @Transactional
    public EventCheckerResponse assignByEmail(int postId, int organizerId, String rawEmail) {
        Post post = postService.getById(postId);
        assertOwner(post.getUser().getId(), organizerId, ONLY_OWNER);

        String email = normalizeEmail(rawEmail);
        User u = userService.getByEmailEntity(email);

        ensureNotAlreadyChecker(postId, u.getId());

        EventChecker ec = new EventChecker();
        ec.setPost(post);
        ec.setUser(u);
        repo.save(ec);
        return mapper.toResponse(ec);
    }

    @Transactional
    public void revokeByEmail(int postId, int organizerId, String rawEmail) {
        Post post = postService.getById(postId);
        assertOwner(post.getUser().getId(), organizerId, ONLY_OWNER);

        String email = normalizeEmail(rawEmail);
        User u = userService.getByEmailEntity(email);

        if (!repo.existsByPost_IdAndUser_Id(postId, u.getId())) {
            throw new NotFoundException(CHECKER_NOT_FOUND);
        }
        repo.deleteByPost_IdAndUser_Id(postId, u.getId());
    }

    // ---------- Helpers ----------

    private void assertOwner(int actualOwnerId, int expectedOwnerId, String errorCode) {
        if (actualOwnerId != expectedOwnerId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorCode);
        }
    }

    private void ensureNotAlreadyChecker(int postId, int userId) {
        if (repo.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new AlreadyExistsException(ALREADY_CHECKER);
        }
    }

    private String normalizeEmail(String rawEmail) {
        String email = rawEmail == null ? "" : rawEmail.trim();
        if (email.isEmpty()) {
            throw new NotFoundException(EMAIL_EMPTY);
        }
        return email.toLowerCase(Locale.ROOT);
    }
}
