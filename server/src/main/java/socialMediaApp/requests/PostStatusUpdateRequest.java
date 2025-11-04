package socialMediaApp.requests;

import lombok.Data;
import socialMediaApp.models.enums.EventStatus;

@Data
public class PostStatusUpdateRequest {
    private EventStatus status; // DRAFT | PUBLISHED | CANCELLED
}
