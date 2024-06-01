package john.LOGIN_SYSTEM.monolith.login;

import jakarta.servlet.http.HttpSession;
import john.LOGIN_SYSTEM.common.dto.UserDTO;
import john.LOGIN_SYSTEM.common.response.ResponseClient;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.session.SessionService;
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
@RequestMapping("/api/v1/login")
class LoginController {
    private final LoginService loginService;
    private final SessionService redisSession;

    @Autowired
    public LoginController(LoginService loginService, SessionService session) {
        this.loginService = loginService;
        this.redisSession = session;
    }


    // VERIFY USER FIRST
    // Sanitize email and password from any malicious attempt
    // Check email format if valid
    // Authenticate user with the provided email and password
    // Return response
    @PostMapping("/")
    public ResponseEntity<ResponseClient> LoginUser(@RequestBody UserDTO request, HttpSession session) {
        String sanitizedEmail = Encode.forHtml(request.getEmail());
        String sanitizedPassword = Encode.forHtml(request.getPassword());

        if (request.getEmail().isEmpty() && request.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.LENGTH_REQUIRED)
                    .body(new ResponseClient(ResponseType.LOGIN_ERROR, "Email and password inputs are empty"));
        }

        if (request.getEmail().isEmpty()){
            return ResponseEntity.status(HttpStatus.LENGTH_REQUIRED)
                    .body(new ResponseClient(ResponseType.LOGIN_ERROR, "Email input is empty"));
        }

        if (request.getPassword().isEmpty()){
            return ResponseEntity.status(HttpStatus.LENGTH_REQUIRED)
                    .body(new ResponseClient(ResponseType.LOGIN_ERROR, "Password input is empty"));
        }

        if (!EmailValidator.getInstance().isValid(sanitizedEmail)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ResponseClient(ResponseType.LOGIN_ERROR, "Invalid email format"));
        }


        var loginVerify = loginService.authenticateUser(sanitizedEmail, sanitizedPassword);

        if (loginVerify.isSuccess()) {
            redisSession.setSession(session, "login-access", true);
        }

        return ResponseEntity.status(loginVerify.getHttpStatus())
                .body(
                        new ResponseClient(switch (loginVerify.getHttpStatus()) {
                            case HttpStatus.BAD_REQUEST -> ResponseType.LOGIN_ERROR;
                            case HttpStatus.OK -> ResponseType.LOGIN_SUCCESS;
                            default -> ResponseType.LOGIN_EXCEPTION;
                        }
                , loginVerify.getMessage()));
    }
}
