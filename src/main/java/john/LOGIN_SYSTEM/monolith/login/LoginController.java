package john.LOGIN_SYSTEM.monolith.login;

import jakarta.servlet.http.HttpSession;
import john.LOGIN_SYSTEM.common.dto.UserDTO;
import john.LOGIN_SYSTEM.common.response.ResponseClient;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.common.response.ResponseUtil;
import john.LOGIN_SYSTEM.session.SessionAttr;
import john.LOGIN_SYSTEM.session.SessionService;
import org.apache.commons.validator.routines.EmailValidator;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/login")
class LoginController {
    private final LoginService loginService;
    private final SessionService redisSession;
    private final ResponseUtil responseUtil;

    @Autowired
    public LoginController(LoginService loginService, SessionService session, ResponseUtil responseUtil) {
        this.loginService = loginService;
        this.redisSession = session;
        this.responseUtil = responseUtil;
    }

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("welcome");
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

        // check session if logged
        if (redisSession.getSession(session, "login-access") != null) {
            return responseUtil.buildResponse(
                    HttpStatus.OK,
                    ResponseType.LOGIN_SUCCESS,
                    "Already log-in. Welcome"
            );
        }

        if (request.getEmail().isEmpty() && request.getPassword().isEmpty()) {
            return responseUtil.buildErrorResponse(
                    HttpStatus.LENGTH_REQUIRED,
                    ResponseType.LOGIN_ERROR,
                    "Email and password inputs are empty");
        }

        if (request.getEmail().isEmpty()) {
            return responseUtil.buildErrorResponse(
                    HttpStatus.LENGTH_REQUIRED,
                    ResponseType.LOGIN_ERROR,
                    "Email input is empty");
        }

        if (request.getPassword().isEmpty()) {
            return responseUtil.buildErrorResponse(
                    HttpStatus.LENGTH_REQUIRED,
                    ResponseType.LOGIN_ERROR,
                    "Password input is empty");
        }

        if (!EmailValidator.getInstance().isValid(sanitizedEmail)) {
            return responseUtil.buildErrorResponse(
                    HttpStatus.NOT_ACCEPTABLE,
                    ResponseType.LOGIN_ERROR,
                    "Invalid email format");
        }

        var loginVerify = loginService.authenticateUser(sanitizedEmail, sanitizedPassword);

        // set session and id.
        if (loginVerify.isSuccess()) {
            String USERID = loginVerify.getDataAccess().getObjectIds().getFirst().toString();
            redisSession.setSession(session,                        // Create
                    SessionAttr.LOGIN_ACCESS_SESSION,               // attr = LoginAccess
                    true);                                          // body

            redisSession.setSession(session,                        // Create
                    SessionAttr.USER_ID_SESSION,                    // attr = UserId
                    USERID);                                        // body
        }

        ResponseType responseType = switch (loginVerify.getHttpStatus()) {
            case HttpStatus.BAD_REQUEST -> ResponseType.LOGIN_ERROR;
            case HttpStatus.OK -> ResponseType.LOGIN_SUCCESS;
            default -> ResponseType.LOGIN_EXCEPTION;
        };

        // HttpStatus.BAD_REQUEST
        // HttpStatus.OK
        return responseUtil.buildResponse(
                loginVerify.getHttpStatus(),
                responseType,
                loginVerify.getMessage());
    }
}
