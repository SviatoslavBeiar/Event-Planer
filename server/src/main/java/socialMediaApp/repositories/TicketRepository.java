
package socialMediaApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import socialMediaApp.models.Ticket;
import socialMediaApp.models.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.Collection;
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



    long countByPost_IdAndStatus(int postId, TicketStatus status);

    @Query("""
        SELECT DATE(t.usedAt), COUNT(t)
        FROM Ticket t
        WHERE t.post.id = :postId
          AND t.status = 'USED'
          AND t.usedAt >= :from
        GROUP BY DATE(t.usedAt)
        ORDER BY DATE(t.usedAt)
    """)
    List<Object[]> countDailyAttendance(
            @Param("postId") int postId,
            @Param("from") LocalDateTime from
    );

    boolean existsByPaymentIntentId(String paymentIntentId);

    boolean existsByCheckoutSessionId(String checkoutSessionId);

    //new
    long countByPost_IdAndStatusIn(int postId, Collection<TicketStatus> statuses);
}
