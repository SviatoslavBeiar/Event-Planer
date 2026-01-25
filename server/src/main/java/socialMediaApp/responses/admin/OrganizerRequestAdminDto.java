package socialMediaApp.responses.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import socialMediaApp.models.enums.OrganizerRequestStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrganizerRequestAdminDto {
    private Long id;
    private OrganizerRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String note;

    private int userId;
    private String userEmail;
    private String userName;
    private String userLastName;
}
