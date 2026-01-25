package socialMediaApp.responses.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import socialMediaApp.models.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostGetResponse {
    private int id;
    private int userId;
    private String userName;
    private String userLastName;

    private String title;
    private String description;
    private String location;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer capacity;

    private Boolean paid;
    private BigDecimal price;
    private String currency;

    private LocalDateTime salesStartAt;
    private LocalDateTime salesEndAt;

    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
