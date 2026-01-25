package socialMediaApp.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import socialMediaApp.models.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    void deleteById(int id);

  //     List<Comment> findAllByUser_Id(int userId);
  //     List<Comment> findAllByPost_Id(int postId);

    @EntityGraph(attributePaths = {"user", "post"})
    List<Comment> findAllBy(); // "усі", але з fetch user+post

    @EntityGraph(attributePaths = {"user", "post"})
    Optional<Comment> findWithUserAndPostById(int id);

    @EntityGraph(attributePaths = {"user", "post"})
    List<Comment> findAllByUser_IdOrderByIdAsc(int userId);

    @EntityGraph(attributePaths = {"user", "post"})
    List<Comment> findAllByPost_IdOrderByIdAsc(int postId);
}