package socialMediaApp.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import socialMediaApp.models.enums.Role;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank
    @Size(min = 2, max = 50)
    private String name;
    @NotBlank @Size(min = 2, max = 50)
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @NotBlank @Size(min = 6, max = 100)
    private String password;

    private boolean organizerRequest;
}
