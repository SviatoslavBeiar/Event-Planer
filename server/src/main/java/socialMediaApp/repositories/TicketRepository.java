// src/main/java/socialMediaApp/repositories/TicketRepository.java
package socialMediaApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import socialMediaApp.models.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    boolean existsByPost_IdAndUser_Id(int postId, int userId);

    Optional<Ticket> findByPost_IdAndUser_Id(int postId, int userId);

    long countByPost_Id(int postId);

    boolean existsByCode(String code);

    List<Ticket> findAllByUser_IdOrderByCreatedAtDesc(int userId);


    Optional<Ticket> findByCode(String code);


}
