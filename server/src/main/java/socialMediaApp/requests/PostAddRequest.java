package socialMediaApp.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostAddRequest {
    private int userId;


    private String title;
    private String description;
    private String location;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer capacity;

    private Boolean paid;          // true/false
    private BigDecimal price;      // 0 або null для free
    private String currency;       // "PLN" by default

    private LocalDateTime salesStartAt;
    private LocalDateTime salesEndAt;
}
