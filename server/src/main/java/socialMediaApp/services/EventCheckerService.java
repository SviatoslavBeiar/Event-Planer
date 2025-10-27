// src/main/java/socialMediaApp/services/EventCheckerService.java
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
import socialMediaApp.responses.EventCheckerResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventCheckerService {

    private final EventCheckerRepository repo;
    private final EventCheckerMapper mapper;
    private final PostService postService;
    private final UserService userService;


    public List<EventCheckerResponse> byPost(int postId, int requesterId) {
        Post post = postService.getById(postId);
        if (post.getUser().getId() != requesterId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_OWNER");
        }
        return mapper.toResponses(repo.findAllByPost_IdOrderByCreatedAtDesc(postId));
    }


    public List<EventCheckerResponse> mine(int userId) {
        return mapper.toResponses(repo.findAllByUser_IdOrderByCreatedAtDesc(userId));
    }

    public boolean amIChecker(int postId, int userId) {
        return repo.existsByPost_IdAndUser_Id(postId, userId);
    }

    @Transactional
    public EventCheckerResponse assign(int postId, int organizerId, int userId) {
        Post post = postService.getById(postId);
        if (post.getUser().getId() != organizerId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_EVENT_OWNER");
        }
        if (repo.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new AlreadyExistsException("Already a checker");
        }
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
        if (post.getUser().getId() != organizerId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_EVENT_OWNER");
        }
        var list = repo.findAllByPost_IdOrderByIdAsc(postId);
        var toDelete = list.stream().filter(e -> e.getUser().getId() == userId).findFirst()
                .orElseThrow(() -> new NotFoundException("Checker not found"));
        repo.delete(toDelete);
    }



    @Transactional
    public EventCheckerResponse assignByEmail(int postId, int organizerId, String rawEmail) {
        Post post = postService.getById(postId);
        if (post.getUser().getId() != organizerId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_EVENT_OWNER");
        }
        String email = rawEmail == null ? "" : rawEmail.trim();
        User u = userService.getByEmailEntity(email);

        if (repo.existsByPost_IdAndUser_Id(postId, u.getId())) {
            throw new AlreadyExistsException("Already a checker");
        }
        EventChecker ec = new EventChecker();
        ec.setPost(post);
        ec.setUser(u);
        repo.save(ec);
        return mapper.toResponse(ec);
    }

    @Transactional
    public void revokeByEmail(int postId, int organizerId, String rawEmail) {
        Post post = postService.getById(postId);
        if (post.getUser().getId() != organizerId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_EVENT_OWNER");
        }
        String email = rawEmail == null ? "" : rawEmail.trim();
        User u = userService.getByEmailEntity(email);

        if (!repo.existsByPost_IdAndUser_Id(postId, u.getId())) {
            throw new NotFoundException("Checker not found");
        }
        repo.deleteByPost_IdAndUser_Id(postId, u.getId());
    }
}
