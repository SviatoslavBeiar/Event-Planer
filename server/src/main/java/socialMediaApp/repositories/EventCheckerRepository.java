// src/main/java/socialMediaApp/repositories/EventCheckerRepository.java
package socialMediaApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import socialMediaApp.models.EventChecker;

import java.util.List;

public interface EventCheckerRepository extends JpaRepository<EventChecker, Integer> {
    boolean existsByPost_IdAndUser_Id(int postId, int userId);
    List<EventChecker> findAllByPost_IdOrderByIdAsc(int postId);
    List<EventChecker> findAllByUser_IdOrderByCreatedAtDesc(int userId);
    void deleteByPost_IdAndUser_Id(int postId, int userId);
    List<EventChecker> findAllByPost_IdOrderByCreatedAtDesc(int postId);
}
