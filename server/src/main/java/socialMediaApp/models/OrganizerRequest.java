package socialMediaApp.models;

import lombok.Getter;
import lombok.Setter;
import socialMediaApp.models.enums.OrganizerRequestStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "organizer_requests",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id")
)
public class OrganizerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrganizerRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "note")
    private String note;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = OrganizerRequestStatus.PENDING;
    }
}
