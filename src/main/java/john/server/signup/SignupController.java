package john.server.signup;

import john.server.common.dto.CheckUserInput;
import john.server.common.dto.ResponseFormat;
import john.server.common.dto.ResponseType;
import john.server.common.dto.UserDTO;
import org.owasp.encoder.Encode;
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


    // CHECK USER'S INPUTS BEFORE SIGNUP
    // Sanitize user inputs from malicious attempt
    // Check email and username format; Return bad response if error occurs
    // Proceed to create new account
    // Return response
    @PostMapping("/create-user")
    public ResponseEntity<ResponseFormat> SignupUser(@Valid @RequestBody UserDTO request) {
        String emailProvided = Encode.forHtml(request.getEmail());
        String usernameProvided = Encode.forHtml(request.getUsername());

        CheckUserInput email = signupService.checkEmail(emailProvided);
        CheckUserInput username = signupService.checkUsername(usernameProvided);

        List<String> errorMessages = Stream.of(email.getMessage(), username.getMessage())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!email.isSuccess() || !username.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            new ResponseFormat(ResponseType.SIGNUP_ERROR,
                                    "ERROR: " + String.join(", ", errorMessages),
                                    HttpStatus.BAD_REQUEST));
        }

        CheckUserInput signupResponse = signupService.signupNewAccount(request);

        if (signupResponse.isSuccess()) {
            // Registration successful
            return ResponseEntity.status(signupResponse.getHttpStatus())
                    .body(
                            new ResponseFormat(ResponseType.SIGNUP_SUCCESS,
                                    signupResponse.getMessage(),
                                    signupResponse.getHttpStatus())
                    );
        } else {
            // Failed to register
            return ResponseEntity.status(signupResponse.getHttpStatus())
                    .body(
                            new ResponseFormat(ResponseType.SIGNUP_ERROR,
                                    signupResponse.getMessage(),
                                    signupResponse.getHttpStatus())
                    );
        }
    }
}

