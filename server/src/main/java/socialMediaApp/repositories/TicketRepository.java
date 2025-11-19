// src/main/java/socialMediaApp/repositories/TicketRepository.java
package socialMediaApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import socialMediaApp.models.Ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    boolean existsByPost_IdAndUser_Id(int postId, int userId);

    Optional<Ticket> findByPost_IdAndUser_Id(int postId, int userId);

    long countByPost_Id(int postId);

    boolean existsByCode(String code);

    List<Ticket> findAllByUser_IdOrderByCreatedAtDesc(int userId);


    Optional<Ticket> findByCode(String code);

    // daily sold per date (createdAt) since :from
    @Query("""
        select function('date', t.createdAt) as day, count(t) as cnt
        from Ticket t
        where t.post.id = :postId and t.createdAt >= :from
        group by function('date', t.createdAt)
        order by function('date', t.createdAt)
    """)
    List<Object[]> countDailyByPost(int postId, LocalDateTime from);


}
