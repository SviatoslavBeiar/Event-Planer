// src/main/java/socialMediaApp/responses/eventchecker/EventCheckerResponse.java
package socialMediaApp.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class EventCheckerResponse {
    private int id;
    private int postId;
    private int userId;
    private String userFullName;
    private String userEmail;
    private LocalDateTime createdAt;
}
