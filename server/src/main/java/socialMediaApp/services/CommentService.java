package socialMediaApp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import socialMediaApp.api.exp.NotFoundException;
import socialMediaApp.mappers.CommentMapper;
import socialMediaApp.models.Comment;
import socialMediaApp.models.Post;
import socialMediaApp.models.User;
import socialMediaApp.repositories.CommentRepository;
import socialMediaApp.requests.CommentAddRequest;
import socialMediaApp.requests.CommentUpdateRequest;
import socialMediaApp.responses.comment.CommentGetResponse;

import java.util.Comparator;
import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final PostService postService;


    @Transactional
    public void add(CommentAddRequest commentAddRequest) {
        User user = userService.getById(commentAddRequest.getUserId());
        Post post = postService.getById(commentAddRequest.getPostId());

        if (commentAddRequest.getDescription() == null || commentAddRequest.getDescription().isBlank()) {
            throw new IllegalArgumentException("Comment description must not be blank");
        }

        Comment comment = commentMapper.addRequestToComment(commentAddRequest);

        if (comment.getUser() == null || comment.getUser().getId() != user.getId()) {
            comment.setUser(user);
        }
        if (comment.getPost() == null || comment.getPost().getId() != post.getId()) {
            comment.setPost(post);
        }

        commentRepository.save(comment);
    }

    public List<CommentGetResponse> getAll() {
        var comments = commentRepository.findAllBy();
        return commentMapper.commentsToResponses(comments);
    }

    public CommentGetResponse getById(int id) {
        var comment = commentRepository.findWithUserAndPostById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found: id=" + id));
        return commentMapper.commentToResponse(comment);
    }

    public List<CommentGetResponse> getAllByPost(int postId) {
        postService.getById(postId);
        var comments = commentRepository.findAllByPost_IdOrderByIdAsc(postId);
        return commentMapper.commentsToResponses(comments);
    }

    public List<CommentGetResponse> getAllByUser(int userId) {
        userService.getById(userId);
        var comments = commentRepository.findAllByUser_IdOrderByIdAsc(userId);
        return commentMapper.commentsToResponses(comments);
    }

    @Transactional
    public void update(int id, CommentUpdateRequest commentUpdateRequest) {
        Comment commentToUpdate = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found: id=" + id));

        if (commentUpdateRequest.getDescription() == null || commentUpdateRequest.getDescription().isBlank()) {
            throw new IllegalArgumentException("Comment description must not be blank");
        }

        commentToUpdate.setDescription(commentUpdateRequest.getDescription());

    }

    @Transactional
    public void delete(int id) {
        if (!commentRepository.existsById(id)) {
            throw new NotFoundException("Comment not found: id=" + id);
        }
        commentRepository.deleteById(id);
    }
}
