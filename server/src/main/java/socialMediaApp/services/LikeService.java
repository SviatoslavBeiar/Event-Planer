package socialMediaApp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import socialMediaApp.api.exp.AlreadyExistsException;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.LikeMapper;
import socialMediaApp.models.Like;
import socialMediaApp.models.Post;
import socialMediaApp.models.User;
import socialMediaApp.repositories.LikeRepository;
import socialMediaApp.requests.LikeRequest;
import socialMediaApp.responses.like.LikeResponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeMapper likeMapper;
    private final UserService userService;
    private final PostService postService;

    public LikeService(LikeRepository likeRepository,
                       LikeMapper likeMapper,
                       UserService userService,
                       PostService postService) {
        this.likeRepository = likeRepository;
        this.likeMapper = likeMapper;
        this.userService = userService;
        this.postService = postService;
    }

    public List<LikeResponse> getAllByPost(int postId) {
        // 404 якщо пост не існує
        postService.getById(postId);
        List<Like> likes = likeRepository.findAllByPost_Id(postId);
        return likeMapper.likesToLikeResponses(likes);
    }

    public List<LikeResponse> getAllByUser(int userId) {
        // 404 якщо користувач не існує
        userService.getById(userId);
        List<Like> likes = likeRepository.findAllByUser_Id(userId);
        return likeMapper.likesToLikeResponses(likes);
    }

    public boolean isLiked(int userId, int postId) {
        // узгоджено з іншими сервісами — кидаємо 404, якщо ресурсів немає
        userService.getById(userId);
        postService.getById(postId);
        return likeRepository.findByUser_IdAndPost_Id(userId, postId).isPresent();
    }

    @Transactional
    public void add(LikeRequest req) {
        User user = userService.getById(req.getUserId());
        Post post = postService.getById(req.getPostId());

        boolean exists = likeRepository.findByUser_IdAndPost_Id(req.getUserId(), req.getPostId()).isPresent();
        if (exists) {
            throw new AlreadyExistsException("Already liked this post");
        }

        Like like = likeMapper.requestToLike(req);
        // нормалізуємо зв’язки на випадок, якщо мапер їх не поставив/поставив інші
        if (like.getUser() == null || like.getUser().getId() != user.getId()) {
            like.setUser(user);
        }
        if (like.getPost() == null || like.getPost().getId() != post.getId()) {
            like.setPost(post);
        }

        likeRepository.save(like);
    }

    @Transactional
    public void delete(LikeRequest req) {
        Like like = likeRepository.findByUser_IdAndPost_Id(req.getUserId(), req.getPostId())
                .orElseThrow(() -> new NotFoundException(
                        "Like not found: userId=" + req.getUserId() + ", postId=" + req.getPostId()
                ));
        likeRepository.delete(like);
    }
}
