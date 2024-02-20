package john.server.forgetpassword;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import john.server.common.dto.UserDTO;
import john.server.common.dto.VerificationCodeDTO;
import john.server.common.response.ResponseClient;
import john.server.common.response.ResponseLayer;
import john.server.common.response.ResponseType;
import john.server.forgetpassword.token.CodeTokenService;
import org.apache.commons.validator.routines.EmailValidator;
import org.bson.types.ObjectId;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/forget-password")
public class ForgetPasswordController {
    private final ForgetPasswordService forgetPasswordService;
    private final CodeTokenService codeToken;

    @Autowired
    public ForgetPasswordController(ForgetPasswordService forgetPasswordService, CodeTokenService codeToken) {
        this.forgetPasswordService = forgetPasswordService;
        this.codeToken = codeToken;
    }


    // CHECK USER INPUT FORMATS
    // Sanitize email from any malicious, check email format
    // Proceed to next method if user input pass cleanup check
    @PostMapping("/validate-email")
    public ResponseEntity<ResponseClient> forgetPasswordUser(@RequestBody UserDTO request, HttpSession session, HttpServletRequest servlet) {
        String sanitizedEmail = Encode.forHtml(request.getEmail());

        if (request.getEmail().isEmpty() || !EmailValidator.getInstance().isValid(sanitizedEmail)) {
            String message = "ERROR: Invalid email format";
            HttpStatus status = HttpStatus.NOT_ACCEPTABLE;

            return ResponseEntity.status(status)
                    .body(new ResponseClient(
                            ResponseType.RESET_PASSWORD_ERROR, message));
        }
        return validateAccount(sanitizedEmail, session);
    }

    // VALIDATE EMAIL
    // Verify email
    // Sent verification code via email
    // Return response
    private ResponseEntity<ResponseClient> validateAccount(String email, HttpSession session) {
        ResponseLayer validateAccount = forgetPasswordService.verifyAccountFirst(email);

        // Return as success if account exist
        if (validateAccount.isSuccess()) {
            session.setAttribute("verification-code", validateAccount.getUserEntity().getId());

            return ResponseEntity.status(validateAccount.getHttpStatus())
                    .body(new ResponseClient(
                            ResponseType.RESET_PASSWORD_SUCCESS,
                            validateAccount.getMessage()));
        } else {
            return ResponseEntity.status(validateAccount.getHttpStatus())
                    .body(new ResponseClient(
                            ResponseType.RESET_PASSWORD_ERROR,
                            validateAccount.getMessage()));
        }
    }


    @PostMapping("/verify-verification-code")
    public void isVerificationCodeValid(@RequestBody VerificationCodeDTO request, HttpSession session) {
        if(request.getVerification() == null) {
            System.out.println("ERROR");
        }
        ObjectId verificationCode = (ObjectId) session.getAttribute("verification-code");
        String code = codeToken.getVerificationCodeById(verificationCode);


        if (!request.getVerification().equals(code)) {
            System.out.println("ERROR code is: " + code);
        } else {
            System.out.println("SUCCESS code is: " + code);
        }

    }
}


