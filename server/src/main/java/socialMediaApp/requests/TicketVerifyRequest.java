package socialMediaApp.requests;

import lombok.Data;

@Data
public class TicketVerifyRequest {
    private int postId;
    private String code;
}
