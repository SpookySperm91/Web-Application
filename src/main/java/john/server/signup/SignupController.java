package john.server.signup;

import john.server.repository_entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/signup")
public class SignupController {
    private final SignupService signupService;

    @Autowired
    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> SignupUser(@RequestBody SignupDTO request) {
        try {
            // Check email if exist
            Optional<UserEntity> emailResponse = signupService.checkEmailExistFirst(request.getEmail());

            if (emailResponse.isPresent()) {
                return ResponseEntity.badRequest().body("ERROR: Email already exist");
            }

            // Proceed to create new account
            Optional<UserEntity> signupResponse = signupService.signupNewAccount(request);

            if (signupResponse.isPresent()) {
                return ResponseEntity.ok("SUCCESS: Registration successful"); // Registration successful
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR: Failed to create an account");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("ERROR: Invalid email format");
        }
    }
}
