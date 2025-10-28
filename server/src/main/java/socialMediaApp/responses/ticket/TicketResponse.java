// src/main/java/socialMediaApp/responses/ticket/TicketResponse.java
package socialMediaApp.responses.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class TicketResponse {
    private int id;
    private String code;

    private int postId;
    private String postTitle;

    private int userId;
    private String userFullName;

    private LocalDateTime createdAt;
}
