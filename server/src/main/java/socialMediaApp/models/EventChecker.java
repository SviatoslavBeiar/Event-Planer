// src/main/java/socialMediaApp/models/EventChecker.java
package socialMediaApp.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "event_checkers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class EventChecker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false) @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist void onCreate() {
        createdAt = LocalDateTime.now(); }

}
