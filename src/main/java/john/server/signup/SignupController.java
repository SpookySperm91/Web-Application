package john.server.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class SignupController {
    private final SignupService signupService;

    @Autowired
    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> SignupUser(@RequestBody DTO request) {

        ResponseEntity<String> emailResponse = signupService.checkEmailExistFirst(request.getEmail());

        if (emailResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return emailResponse;
        }

        ResponseEntity<String> signupResponse = signupService.signupNewAccount(request);

        if (signupResponse.getStatusCode() == HttpStatus.OK) {
            return signupResponse; // Registration successful
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create an account. Please try again later.");
        }
    }
}
