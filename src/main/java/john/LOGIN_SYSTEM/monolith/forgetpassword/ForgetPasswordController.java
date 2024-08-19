package john.LOGIN_SYSTEM.monolith.forgetpassword;

import jakarta.servlet.http.HttpSession;
import john.LOGIN_SYSTEM.common.dto.PasswordDTO;
import john.LOGIN_SYSTEM.common.dto.UserDTO;
import john.LOGIN_SYSTEM.common.dto.VerificationCodeDTO;
import john.LOGIN_SYSTEM.common.response.ResponseClient;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.common.response.ResponseUtil;
import john.LOGIN_SYSTEM.session.SessionAttr;
import john.LOGIN_SYSTEM.session.SessionService;
import org.apache.commons.validator.routines.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/forget-password")
public class ForgetPasswordController {
    private final ForgetPasswordService serviceLayer;
    private final SessionService redisSession;
    private final ResponseUtil responseUtil;

    @Autowired
    public ForgetPasswordController(ForgetPasswordService serviceLayer, SessionService redisSession, ResponseUtil responseUtil) {
        this.serviceLayer = serviceLayer;
        this.redisSession = redisSession;
        this.responseUtil = responseUtil;
    }


    // CHECK USER INPUT FORMATS
    // Sanitize email from any malicious, check email format
    // Proceed to next method if user input pass cleanup check
    @PostMapping("/")
    public ResponseEntity<ResponseClient> forgetPasswordUser(@RequestBody UserDTO request, HttpSession session) {
        String sanitizedEmail = Encode.forHtml(request.getEmail());

        if (request.getEmail().isEmpty() || !EmailValidator.getInstance().isValid(sanitizedEmail)) {
            return responseUtil.buildErrorResponse(
                    HttpStatus.NOT_ACCEPTABLE,
                    ResponseType.RESET_PASSWORD_ERROR,
                    "ERROR: Invalid email format");
        }
        return validateAccount(sanitizedEmail, session);
    }

    // Verify email
    // Sent verification code via email
    // Return response
    @NotNull
    private ResponseEntity<ResponseClient> validateAccount(String email, HttpSession session) {
        var validateAccount = serviceLayer.verifyAccountFirst(email, session);

        // Generate entry session for reset password
        if (validateAccount.isSuccess()) {
            int EXPIRATION_IN_MINUTES = 25;
            String TOKEN = validateAccount.getDataAccess().getStrings().getFirst();
            String ID = validateAccount.getDataAccess().getObjectIds().getFirst().toString();

            redisSession.setSession(session,                      // Create
                    SessionAttr.VERIFICATION_CODE_SESSION,         // attr = VerificationCode
                    TOKEN,                                         // body
                    EXPIRATION_IN_MINUTES);                        // expire

            redisSession.setSession(session,                       // Create
                    SessionAttr.USER_ID_SESSION,                   // attr = UserId
                    ID);                                           // expire
        }
        // HttpStatus.NOT_FOUND
        // HttpStatus.BAD_REQUEST
        // HttpStatus.OK
        // Return as success if account exist
        return responseUtil.buildResponse(
                validateAccount.getHttpStatus(),
                validateAccount.getType(),
                validateAccount.getMessage());
    }


    // VERIFY VERIFICATION CODE
    // verify if code is valid
    // return response
    @PostMapping("/verification")
    public ResponseEntity<ResponseClient> isVerificationCodeValid(@RequestBody VerificationCodeDTO request, HttpSession session) {
        if (request.getVerification().isEmpty()) {
            return responseUtil.buildErrorResponse(
                    HttpStatus.NOT_ACCEPTABLE,
                    ResponseType.VERIFICATION_ERROR,
                    "user input is empty");
        }

        if (redisSession.getSession(session, SessionAttr.VERIFICATION_CODE_SESSION) == null) {
            return responseUtil.buildErrorResponse(
                    HttpStatus.FORBIDDEN,
                    ResponseType.VERIFICATION_EXCEPTION,
                    "Forbidden: Session is expired or does not exist");
        }

        var validCode = serviceLayer.matchVerification(request.getVerification(), request.getEmail(), session);

        // Success. remove former session, generate session for change-password
        if (validCode.isSuccess()) {
            redisSession.removeSession(session,                         // Remove
                    SessionAttr.VERIFICATION_CODE_SESSION);             // attr = VerificationCode

            redisSession.setSession(session,                            // Create
                    SessionAttr.CHANGE_PASSWORD_SESSION,                // attr = ChangePassword
                    true,                                               // body
                    10);                                                // expire
        }

        // HttpStatus.NOT_FOUND
        // HttpStatus.BAD_REQUEST
        // HttpStatus.SUCCESS
        return responseUtil.buildErrorResponse(
                validCode.getHttpStatus(),
                validCode.getType(),
                validCode.getMessage());
    }


    // CHANGE PASSWORD
    // Check if session exist, proceed to change password if so
    // Return response
    @PutMapping("/change-password")
    public ResponseEntity<ResponseClient> changePassword(@RequestBody PasswordDTO request, HttpSession session) {
        Boolean sessionValue = (Boolean) redisSession.getSession(session, SessionAttr.CHANGE_PASSWORD_SESSION);
        boolean isSessionValid = sessionValue != null && sessionValue; // Defense against NullPointerException

        if (!isSessionValid) {
            System.out.println("Invalid session");
            return responseUtil.buildErrorResponse(
                    HttpStatus.FORBIDDEN,
                    ResponseType.RESET_PASSWORD_EXCEPTION,
                        "Forbidden: Session is expired or does not exist");
        }

        // Retrieve user id from session
        // Perform password-reset
        String userId = (String) redisSession.getSession(session, SessionAttr.USER_ID_SESSION);
        var passwordReset = serviceLayer.resetPassword(userId, request);

        // Success. remove session attributes
        if (passwordReset.isSuccess()) {
            redisSession.removeSession(session, SessionAttr.USER_ID_SESSION);             // Remove, attr = UserId
            redisSession.removeSession(session, SessionAttr.CHANGE_PASSWORD_SESSION);     // Remove, attr = ChangePassword
        }

        // HttpStatus.NOT_FOUND
        // HttpStatus.BAD_REQUEST
        // HttpStatus.OK
        // HttpStatus.SERVICE_UNAVAILABLE
        return responseUtil.buildResponse(
                passwordReset.getHttpStatus(),
                passwordReset.getType(),
                passwordReset.getMessage());
    }
}
