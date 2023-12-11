package john.server.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signup")
public class SignupController {
    private final SignupService signupService;

    @Autowired
    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping ("/create")
    public ResponseEntity<String> SignupUser(@RequestBody DTO request) {
        // Check email if exist
        ResponseEntity<String> emailResponse = signupService.checkEmailExistFirst(request.getEmail());

        if (emailResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return ResponseEntity.badRequest().body("ERROR: Email already exist");
        }



        ResponseEntity<String> signupResponse = signupService.signupNewAccount(request);

        if (signupResponse.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok("SUCCESS: Registration successful"); // Registration successful
        } else {
            return ResponseEntity.status(signupResponse.getStatusCode())
                    .body("ERROR: Failed to create an account.");
        }
    }

}
