package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.PostMapper;
import socialMediaApp.models.Post;
import socialMediaApp.models.User;
import socialMediaApp.models.enums.EventStatus;
import socialMediaApp.models.enums.Role;
import socialMediaApp.repositories.PostRepository;
import socialMediaApp.requests.PostAddRequest;
import socialMediaApp.responses.post.PostGetResponse;
import socialMediaApp.responses.user.UserFollowingResponse;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserService userService;



    @Transactional
    public PostGetResponse updateStatusAsOrganizer(int postId, String organizerEmail, EventStatus newStatus) {
        int organizerId = userService.getByEmailEntity(organizerEmail).getId();
        return updateStatus(postId, organizerId, newStatus);
    }
    @Transactional
    public int addAsOrganizer(PostAddRequest request, String organizerEmail) {
        User me = userService.getByEmailEntity(organizerEmail); // кине 404/NotFound якщо нема
        if (me.getRole() != Role.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_ORGANIZER_CAN_ADD");
        }
        return addForAuthor(request, me);
    }

    @Transactional
    public int addForAuthor(PostAddRequest request, User author) {
        Post post = postMapper.postAddRequestToPost(request);

        post.setUser(author);
        postRepository.save(post);
        return post.getId();
    }


    @Transactional
    public PostGetResponse updateStatus(int postId, int organizerId, EventStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("STATUS_REQUIRED");
        }

        Post post = getById(postId);


        if (post.getUser().getId() != organizerId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_EVENT_OWNER");
        }

        EventStatus old = post.getStatus();
        if (old == newStatus) {

            return postMapper.postToGetResponse(post);
        }


        if (old == EventStatus.CANCELLED && newStatus != EventStatus.CANCELLED) {
            throw new IllegalStateException("CANCELLED_IS_FINAL");
        }


        post.setStatus(newStatus); // JPA dirty-checking
        return postMapper.postToGetResponse(post);
    }



    public List<PostGetResponse> getAll() {
        List<Post> posts = postRepository
                .findAllByStatusIn(List.of(EventStatus.CANCELLED, EventStatus.PUBLISHED)
                        , Sort.by(Sort.Direction.DESC, "id"));
        return postMapper.postsToGetResponses(posts);
    }


    public PostGetResponse getResponseById(int id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: id=" + id));
        return postMapper.postToGetResponse(post);
    }

    public Post getById(int id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: id=" + id));
    }

    public List<PostGetResponse> getAllByUser(int userId) {
        userService.getById(userId);
        List<Post> userPosts = postRepository.findAllByUser_IdOrderByIdDesc(userId);
        return postMapper.postsToGetResponses(userPosts);
    }

    public List<PostGetResponse> getByUserFollowing(int userId) {
        List<UserFollowingResponse> follows = userService.getUserFollowing(userId);

        List<Integer> userIds = follows.stream()
                .map(UserFollowingResponse::getUserId)
                .toList();

        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Post> posts = postRepository.findAllByUserIdInAndStatusIn(
                userIds,
                List.of(EventStatus.CANCELLED, EventStatus.PUBLISHED),
                Sort.by(Sort.Direction.DESC, "id")
        );

        return postMapper.postsToGetResponses(posts);
    }

    @Transactional
    public int add(PostAddRequest postAddRequest) {

        User author = userService.getById(postAddRequest.getUserId());

        Post post = postMapper.postAddRequestToPost(postAddRequest);


        if (post.getUser() == null || post.getUser().getId() != author.getId()) {
            post.setUser(author);
        }

        postRepository.save(post);
        return post.getId();
    }

    @Transactional
    public void delete(int id) {
        if (!postRepository.existsById(id)) {
            throw new NotFoundException("Post not found: id=" + id);
        }
        postRepository.deleteById(id);
    }
}
