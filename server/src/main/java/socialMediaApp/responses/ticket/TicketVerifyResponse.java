package socialMediaApp.responses.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import socialMediaApp.models.enums.TicketStatus;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class TicketVerifyResponse {
    private boolean valid;
    private String message;

    private Integer ticketId;
    private String code;
    private TicketStatus status;

    private Integer ownerUserId;
    private String ownerFullName;

    private Integer postId;
    private String postTitle;

    private LocalDateTime verifiedAt;
}
