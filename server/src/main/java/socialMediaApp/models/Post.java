package socialMediaApp.models;

import lombok.Getter;
import lombok.Setter;
import socialMediaApp.models.enums.EventStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "posts")
public class Post {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "description")
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
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
    private Boolean paid;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "sales_start_at")
    private LocalDateTime salesStartAt;

    @Column(name = "sales_end_at")
    private LocalDateTime salesEndAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;

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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    Set<Like> likes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    Set<PostImage> postImages;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    Set<Comment> comments;
}
