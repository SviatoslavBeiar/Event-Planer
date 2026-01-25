package socialMediaApp.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import socialMediaApp.models.EventChecker;

import java.util.List;

public interface EventCheckerRepository extends JpaRepository<EventChecker, Integer> {
    //List<EventChecker> findAllByPost_IdOrderByIdAsc(int postId);

    boolean existsByPost_IdAndUser_Id(int postId, int userId);

    @EntityGraph(attributePaths = {"user"})
    List<EventChecker> findAllByUser_IdOrderByCreatedAtDesc(int userId);

    void deleteByPost_IdAndUser_Id(int postId, int userId);

    @EntityGraph(attributePaths = {"user"})
    List<EventChecker> findAllByPost_IdOrderByCreatedAtDesc(int postId);
}
