package john.server.login;

import john.server.common.dto.CheckUserInput;
import john.server.common.dto.ResponseFormat;
import john.server.common.dto.ResponseType;
import john.server.common.dto.UserDTO;
import org.apache.commons.validator.routines.EmailValidator;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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


    // VERIFY USER FIRST
    // Sanitize email and password from any malicious attempt
    // Check email format if valid
    // Authenticate user with the provided email and password
    // Return response
    @PostMapping("/login-user")
    public ResponseEntity<ResponseFormat> LoginUser(@RequestBody UserDTO request) {
        String sanitizedEmail = Encode.forHtml(request.getEmail());
        String sanitizedPassword = Encode.forHtml(request.getPassword());

        if (request.getEmail() == null || !EmailValidator.getInstance().isValid(sanitizedEmail)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(
                            new ResponseFormat(ResponseType.LOGIN_ERROR,
                                    "Invalid email format",
                                    HttpStatus.NOT_ACCEPTABLE));
        }

        CheckUserInput loginVerify = loginService.authenticateUser(sanitizedEmail, sanitizedPassword);

        if (loginVerify.isSuccess()) {
            return ResponseEntity.status(loginVerify.getHttpStatus())
                    .body(
                            new ResponseFormat(ResponseType.LOGIN_SUCCESS,
                                    loginVerify.getMessage(),
                                    loginVerify.getHttpStatus()));
        }

        return ResponseEntity.status(loginVerify.getHttpStatus())
                .body(
                        new ResponseFormat(ResponseType.LOGIN_ERROR,
                                loginVerify.getMessage(),
                                loginVerify.getHttpStatus()));
    }
}
