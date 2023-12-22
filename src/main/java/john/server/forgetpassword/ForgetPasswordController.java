package john.server.forgetpassword;

import john.server.common.dto.CheckUserInput;
import john.server.common.dto.PasswordDTO;
import org.apache.commons.validator.routines.EmailValidator;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PutMapping("/reset-password")
    public ResponseEntity<String> checkValidityAndResetPassword(@RequestBody PasswordDTO request) {
        // Clean email and password from any malicious
        String email = Encode.forHtml(request.getEmail());
        String password = Encode.forHtml(request.getPassword());

        // Check email format
        if (request.getEmail() == null || !EmailValidator.getInstance().isValid(email)) {
            return ResponseEntity.badRequest().body("ERROR: Invalid email format");
        }
        // Validate email and reset password
        return validateAndResetPassword(email, password, request);
    }

    // Validate email and reset password
    private ResponseEntity<String> validateAndResetPassword(String email, String password, PasswordDTO request) {
        // Verify email and check password
        CheckUserInput validate = forgetPasswordService.verifyAccountFirst(email, password);
        if (!validate.isSuccess()) {
            return ResponseEntity.badRequest().body("ERROR: " + validate.getMessage());
        }

        // Reset Password if validation is true
        try {
            CheckUserInput resetPassword = forgetPasswordService
                    .resetPassword(validate.getUserEntity(), request.getNewPassword());
            // Return as success
            if (resetPassword.isSuccess()) {
                String username = validate.getUserEntity().getUsername();
                return ResponseEntity.ok().body("SUCCESS: " + username + " " + resetPassword.getMessage());
            }
            //Return as fail
            return ResponseEntity.badRequest().body("ERROR: " + resetPassword.getMessage());

            // Catch exception
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ERROR: " + e.getMessage());
        }
    }
}
