package socialMediaApp.api.exp;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

// src/main/java/.../api/ApiError.java
@Data
@Builder
public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;    // "Bad Request"
    private String message;  // людське повідомлення
    private String path;     // URI
    private String code;     // ваш короткий код ("USER_NOT_FOUND")
    private Map<String, String> fieldErrors; // для @Valid
}
