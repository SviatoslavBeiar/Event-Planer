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

    // ✅ ДОДАНО: статус квитка
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = TicketStatus.ACTIVE; // дефолт
    }
}
