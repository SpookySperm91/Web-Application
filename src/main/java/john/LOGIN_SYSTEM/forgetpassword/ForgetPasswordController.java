package john.LOGIN_SYSTEM.forgetpassword;

import jakarta.servlet.http.HttpSession;
import john.LOGIN_SYSTEM.common.dto.PasswordDTO;
import john.LOGIN_SYSTEM.common.dto.UserDTO;
import john.LOGIN_SYSTEM.common.dto.VerificationCodeDTO;
import john.LOGIN_SYSTEM.common.response.ResponseClient;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.session.SessionService;
import org.apache.commons.validator.routines.EmailValidator;
import org.bson.types.ObjectId;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/forget-password")
class ForgetPasswordController {
    private final ForgetPasswordService serviceLayer;
    private final SessionService redisSession;

    @Autowired
    public ForgetPasswordController(ForgetPasswordService serviceLayer, SessionService redisSession) {
        this.serviceLayer = serviceLayer;
        this.redisSession = redisSession;
    }


    // CHECK USER INPUT FORMATS
    // Sanitize email from any malicious, check email format
    // Proceed to next method if user input pass cleanup check
    @PostMapping("/")
    public ResponseEntity<ResponseClient> forgetPasswordUser(@RequestBody UserDTO request, HttpSession session) {
        String sanitizedEmail = Encode.forHtml(request.getEmail());

        if (request.getEmail().isEmpty() || !EmailValidator.getInstance().isValid(sanitizedEmail)) {
            String message = "ERROR: Invalid email format";
            HttpStatus status = HttpStatus.NOT_ACCEPTABLE;

            return ResponseEntity.status(status)
                    .body(new ResponseClient(ResponseType.RESET_PASSWORD_ERROR, message));
        }
        return validateAccount(sanitizedEmail, session);
    }

    // Verify email
    // Sent verification code via email
    // Return response
    private ResponseEntity<ResponseClient> validateAccount(String email, HttpSession session) {
        var validateAccount = serviceLayer.verifyAccountFirst(email, session);

        // Return as success if account exist
        if (validateAccount.isSuccess()) {
            return ResponseEntity.status(validateAccount.getHttpStatus())
                    .body(new ResponseClient(
                            ResponseType.RESET_PASSWORD_SUCCESS,
                            validateAccount.getMessage()));
        } else {
            // Email doesnt exist
            return ResponseEntity.status(validateAccount.getHttpStatus())
                    .body(new ResponseClient(
                            ResponseType.RESET_PASSWORD_ERROR,
                            validateAccount.getMessage()));
        }
    }


    // VERIFY VERIFICATION CODE
    // verify if code is valid
    // return response
    @PostMapping("/verification")
    public ResponseEntity<ResponseClient> isVerificationCodeValid(@RequestBody VerificationCodeDTO request, HttpSession session) {
        if(session.getAttribute("verification-code") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseClient(
                            ResponseType.VERIFICATION_EXCEPTION,
                            "Forbidden: Session is expired or does not exist"));
        }

        if (request.getVerification().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ResponseClient(
                            ResponseType.VERIFICATION_ERROR,
                            "user input is empty"));
        }

        var validCode = serviceLayer.matchVerification(request.getVerification(), session);

        // Return error if fail
        if (!validCode.isSuccess()) {
            return ResponseEntity.status(validCode.getHttpStatus())
                    .body(new ResponseClient(
                            ResponseType.VERIFICATION_ERROR,
                            validCode.getMessage()));
        }
        // else ->
        // Delete verification session. Generate session for change-password. return response
        redisSession.removeSession(session, "verification-code");
        redisSession.setSession(session, "change-password", true, 5);

        return ResponseEntity.status(validCode.getHttpStatus())
                .body(new ResponseClient(
                        ResponseType.VERIFICATION_SUCCESS,
                        validCode.getMessage()));
    }


    // CHANGE PASSWORD
    // Check if session exist, proceed to change password if so
    // Return response
    @PutMapping("/change-password")
    public ResponseEntity<ResponseClient> changePassword(@RequestBody PasswordDTO request, HttpSession session) {
        Boolean sessionValue = (Boolean) redisSession.getSession(session, "change-password");
        boolean isSessionValid = sessionValue != null && sessionValue; // Defense against NullPointerException

        if (!isSessionValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ResponseClient(ResponseType.RESET_PASSWORD_EXCEPTION,
                            "Forbidden: Session is expired or does not exist"));
        }

        // Retrieve user id from session
        // Perform password-reset
        ObjectId userId = (ObjectId) redisSession.getSession(session, "user-id");
        var passwordReset = serviceLayer.resetPassword(userId, request.getNewPassword());

        // Success. remove session attributes
        if (passwordReset.isSuccess()) {
            redisSession.removeSession(session, "user-id");
            redisSession.removeSession(session, "change-password");

            return ResponseEntity.status(passwordReset.getHttpStatus())
                    .body(new ResponseClient(
                            ResponseType.RESET_PASSWORD_SUCCESS,
                            passwordReset.getMessage()));
        }
        // Fail
        else {
            return ResponseEntity.status(passwordReset.getHttpStatus())
                    .body(new ResponseClient(
                            ResponseType.RESET_PASSWORD_ERROR,
                            passwordReset.getMessage()));
        }
    }
}
