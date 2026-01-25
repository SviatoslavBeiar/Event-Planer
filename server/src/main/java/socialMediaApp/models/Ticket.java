// src/main/java/socialMediaApp/models/Ticket.java
package socialMediaApp.models;

import lombok.Getter;
import lombok.Setter;
import socialMediaApp.models.enums.TicketStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "tickets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"})
)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime usedAt;


    @Column(name = "payment_intent_id", unique = true)
    private String paymentIntentId;

    @Column(name = "checkout_session_id", unique = true)
    private String checkoutSessionId;

    @Column(name = "payment_status", length = 32, nullable = false)
    private String paymentStatus; // FREE / PAID

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = TicketStatus.ACTIVE;
        if (paymentStatus == null) paymentStatus = "FREE";
    }

}
