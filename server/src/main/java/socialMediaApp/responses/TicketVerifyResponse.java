package socialMediaApp.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import socialMediaApp.models.enums.TicketStatus;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class TicketVerifyResponse {
    private boolean valid;           // чи валідний для цього івенту і активний
    private String message;          // причина чому ні, або “OK”

    private Integer ticketId;
    private String code;
    private TicketStatus status;

    private Integer ownerUserId;
    private String ownerFullName;

    private Integer postId;
    private String postTitle;

    private LocalDateTime verifiedAt;
}
