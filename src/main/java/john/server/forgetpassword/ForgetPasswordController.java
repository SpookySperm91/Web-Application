package john.server.forgetpassword;

import john.server.common.dto.CheckUserInput;
import john.server.common.dto.PasswordDTO;
import john.server.common.dto.ResponseFormat;
import john.server.common.dto.ResponseType;
import org.apache.commons.validator.routines.EmailValidator;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/forget-password")
public class ForgetPasswordController {
    private final ForgetPasswordService forgetPasswordService;

    @Autowired
    public ForgetPasswordController(ForgetPasswordService forgetPasswordService) {
        this.forgetPasswordService = forgetPasswordService;
    }


    // CHECK USER INPUT FORMATS
    // Sanitize email and password from any malicious, check email format
    // Proceed to next method if user inputs pass cleanup check
    @PutMapping("/reset-password")
    public ResponseEntity<ResponseFormat> ForgetPasswordUser(@RequestBody PasswordDTO request) {
        String email = Encode.forHtml(request.getEmail());
        String password = Encode.forHtml(request.getPassword());

        if (request.getEmail().isEmpty() || !EmailValidator.getInstance().isValid(email)) {
            String message = "ERROR: Invalid email format";
            HttpStatus status = HttpStatus.NOT_ACCEPTABLE;

            return ResponseEntity.status(status).body(
                    new ResponseFormat(
                            ResponseType.RESET_PASSWORD_ERROR, message, status));
        }
        return validateAndResetPassword(email, password, request);
    }


    // VALIDATE EMAIL AND RESET PASSWORD
    // Verify email and check password first
    // Proceed to reset password if validation is true
    // Return response
    private ResponseEntity<ResponseFormat> validateAndResetPassword(String email, String password, PasswordDTO request) {
        CheckUserInput validateAccount = forgetPasswordService.verifyAccountFirst(email, password);

        if (!validateAccount.isSuccess()) {
            return ResponseEntity.status(validateAccount.getHttpStatus())
                    .body(
                            new ResponseFormat(
                                    ResponseType.RESET_PASSWORD_ERROR,
                                    validateAccount.getMessage(),
                                    validateAccount.getHttpStatus()));
        }

        CheckUserInput changePassword = forgetPasswordService.resetPassword
                (validateAccount.getUserEntity(), request.getNewPassword());

        // Return as success
        if (changePassword.isSuccess()) {
            String username = validateAccount.getUserEntity().getUsername();

            return ResponseEntity.status(changePassword.getHttpStatus())
                    .body(
                            new ResponseFormat(
                                    ResponseType.RESET_PASSWORD_SUCCESS,
                                    username + " " + changePassword.getMessage(),
                                    changePassword.getHttpStatus()));
        }
        //Return as fail
        return ResponseEntity.status(changePassword.getHttpStatus())
                .body(
                        new ResponseFormat(
                                ResponseType.RESET_PASSWORD_ERROR,
                                changePassword.getMessage(),
                                changePassword.getHttpStatus()));
    }
}

