package socialMediaApp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.PostMapper;
import socialMediaApp.models.Post;
import socialMediaApp.models.User;
import socialMediaApp.repositories.PostRepository;
import socialMediaApp.requests.PostAddRequest;
import socialMediaApp.responses.post.PostGetResponse;
import socialMediaApp.responses.user.UserFollowingResponse;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserService userService;

    public PostService(PostRepository postRepository, PostMapper postMapper, UserService userService) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.userService = userService;
    }

    public List<PostGetResponse> getAll() {
        List<Post> posts = postRepository.findAll();

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

        List<Post> posts = postRepository.findAllByUserIds(userIds);

        posts.sort(Comparator.comparing(Post::getId).reversed());

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
