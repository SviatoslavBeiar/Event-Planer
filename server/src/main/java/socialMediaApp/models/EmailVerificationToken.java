package socialMediaApp.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "email_verification_tokens",
       uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="token", nullable=false, unique=true, length=80)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(name="used_at")
    private LocalDateTime usedAt;

    @PrePersist
    void onCreate() {
        if (expiresAt == null) expiresAt = LocalDateTime.now().plusHours(24);
    }
}
