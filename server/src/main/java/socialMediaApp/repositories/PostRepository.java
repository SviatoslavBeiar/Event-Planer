package socialMediaApp.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import socialMediaApp.models.Post;
import socialMediaApp.models.enums.EventStatus;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    List<Post> findAllByUser_IdOrderByIdDesc(int userId);

    void deleteById(int id);

    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds ORDER BY p.id DESC")
    List<Post> findAllByUserIds(@Param("userIds") List<Integer> userIds);



    List<Post> findAllByStatusIn(List<EventStatus> statuses, Sort sort);

    List<Post> findAllByUserIdInAndStatusIn(
            List<Integer> userIds,
            List<EventStatus> statuses,
            Sort sort
    );


}