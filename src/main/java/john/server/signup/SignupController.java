package john.server.signup;

import john.server.common.dto.UserDTO;
import john.server.common.dto.CheckUserInput;
import john.server.repository_entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/signup")
public class SignupController {
    private final SignupService signupService;

    @Autowired
    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/create-user")
    public ResponseEntity<String> SignupUser(@Valid @RequestBody UserDTO request) {
        // Check user inputs for validation
        CheckUserInput email = signupService.checkEmail(request.getEmail());
        CheckUserInput username = signupService.checkUsername(request.getUsername());

        List<String> errorMessages = Stream.of(email.getMessage(), username.getMessage())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!email.isSuccess() || !username.isSuccess()) {
            return ResponseEntity.badRequest().body("ERROR: " + String.join(", ", errorMessages));
        }

        // Proceed to create new account
        Optional<UserEntity> signupResponse = signupService.signupNewAccount(request);

        if (signupResponse.isPresent()) {
            return ResponseEntity.ok("SUCCESS: Registration successful"); // Registration successful
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR: Failed to create an account");
        }
    }
}

