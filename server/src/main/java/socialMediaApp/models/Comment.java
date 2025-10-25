package socialMediaApp.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import socialMediaApp.models.enums.EventStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "description")
    private String description;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "post_id")
    Post post;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;


    @Column(name = "title")
    private String title;

    @Column(name = "location")
    private String location;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "paid")
    private Boolean paid;               // true = платний, false = безкоштовний

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;           // 0 або null для free

    @Column(name = "currency", length = 3)
    private String currency;            // "USD","EUR","PLN" тощо

    @Column(name = "sales_start_at")
    private LocalDateTime salesStartAt; // опц. коли стартує продаж

    @Column(name = "sales_end_at")
    private LocalDateTime salesEndAt;   // опц. коли закінчується продаж

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;         // DRAFT/PUBLISHED/CANCELLED (опц.)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) status = EventStatus.DRAFT;
        if (paid == null) paid = Boolean.FALSE;
        if (currency == null) currency = "PLN";
        if (price == null) price = BigDecimal.ZERO;
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}
