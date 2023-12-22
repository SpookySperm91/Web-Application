package john.server.login;

import john.server.common.dto.UserDTO;
import john.server.common.dto.CheckUserInput;
import org.apache.commons.validator.routines.EmailValidator;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/login")
public class LoginController {
    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login-user")
    public ResponseEntity<String> verifyUser(@RequestBody UserDTO request) {
        String sanitizedEmail = Encode.forHtml(request.getEmail());
        String sanitizedPassword = Encode.forHtml(request.getPassword());

        // Check if the email is valid
        if (request.getEmail() == null || !EmailValidator.getInstance().isValid(sanitizedEmail)) {
            return ResponseEntity.badRequest().body("ERROR: Invalid email format");
        }

        CheckUserInput loginVerify = loginService.authenticateUser(sanitizedEmail, sanitizedPassword);

        if (loginVerify.isSuccess()) {
            return ResponseEntity.ok().body("SUCCESS: " + loginVerify.getMessage());
        }

        return ResponseEntity.badRequest().body("ERROR: " + loginVerify.getMessage());
    }
}
