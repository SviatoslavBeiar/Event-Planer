package socialMediaApp.requests;

import lombok.Data;

@Data
public class EventCheckerAssignRequest {
    private int postId;
    private int userId; // кого призначаємо перевіряючим
}
