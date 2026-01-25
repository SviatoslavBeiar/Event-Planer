package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeMapper likeMapper;
    private final UserService userService;
    private final PostService postService;


    public List<LikeResponse> getAllByPost(int postId) {
        postService.getById(postId);
        List<Like> likes = likeRepository.findAllByPost_Id(postId);
        return likeMapper.likesToLikeResponses(likes);
    }

    public List<LikeResponse> getAllByUser(int userId) {

        userService.getById(userId);
        List<Like> likes = likeRepository.findAllByUser_Id(userId);
        return likeMapper.likesToLikeResponses(likes);
    }

    public boolean isLiked(int userId, int postId) {

        userService.getById(userId);
        postService.getById(postId);
        return likeRepository.findByUser_IdAndPost_Id(userId, postId).isPresent();
    }

    @Transactional
    public void add(LikeRequest req) {
        User user = userService.getById(req.getUserId());
        Post post = postService.getById(req.getPostId());

        if (likeRepository.existsByUser_IdAndPost_Id(user.getId(), post.getId())) {
            throw new AlreadyExistsException("Already liked this post");
        }

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
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
