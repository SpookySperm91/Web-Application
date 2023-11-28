package john.server.signup;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DTO {
    private String username;
    private String email;
    private String password;
}
