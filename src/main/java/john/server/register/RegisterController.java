package john.server.register;

import john.server.common.components.AccountLock;
import john.server.common.components.PasswordStrength;
import john.server.common.dto.UserDTO;
import john.server.common.response.*;
import john.server.register.token.LinkToken;
import john.server.register.token.LinkTokenService;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/register")
public class RegisterController {
    private final RegisterService registerService;
    private final PasswordStrength passwordStrength;
    private final LinkTokenService tokenService;
    private final AccountLock accountLock;
    private final ResponseTerminal terminal;

    @Autowired
    public RegisterController(RegisterService signupService,
                              PasswordStrength passwordStrength,
                              LinkTokenService tokenService, AccountLock accountLock, ResponseTerminal terminal) {
        this.registerService = signupService;
        this.passwordStrength = passwordStrength;
        this.tokenService = tokenService;
        this.accountLock = accountLock;
        this.terminal = terminal;
    }


    // CHECK USER'S INPUTS BEFORE SIGNUP
    // Sanitize user inputs from malicious attempt
    // Check email and username format; Return bad response if error occurs
    // Proceed to create new account
    // Return response
    @PostMapping("/create-user")
    public ResponseEntity<ResponseClient> signupUser(@Valid @RequestBody UserDTO request) throws Exception {
        String sanitizedEmail = Encode.forHtml(request.getEmail());
        String sanitizedUsername = Encode.forHtml(request.getUsername());
        String sanitizedPassword = Encode.forHtml(request.getPassword());

        ResponseLayer email = registerService.checkEmail(sanitizedEmail);
        ResponseLayer username = registerService.checkUsername(sanitizedUsername);
        ResponseLayer password = passwordStrength.checkPassword(sanitizedPassword);

        if (!email.isSuccess() || !username.isSuccess() || !password.isSuccess()) {
            List<String> errorMessages = Stream.of(email.getMessage(), username.getMessage(), password.getMessage())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            new ResponseClient(ResponseType.SIGNUP_ERROR,
                                    "ERROR: " + String.join(", ", errorMessages)));
        }


        // Create token for email verification
        ResponseLayer signupResponse = registerService.verificationProcess(sanitizedUsername, sanitizedEmail, sanitizedPassword);

        if (signupResponse.isSuccess()) {
            // Link sent
            return ResponseEntity.status(signupResponse.getHttpStatus())
                    .body(
                            new ResponseClient(ResponseType.SIGNUP_PENDING,
                                    signupResponse.getMessage()));
        } else {
            // Email doesnt exist
            throw new Exception("ERROR: Email does not exist");
        }
    }


    // VERIFY ACCOUNT
    // Check if link is expire or valid
    // Enable locked account
    // Delete the link
    @GetMapping("/verify/{verification}")
    public ResponseEntity<ResponseClient> verificationProcess(@PathVariable String verification) {
        Optional<LinkToken> token = tokenService.getVerificationLink(verification);

        if (token.isPresent()) {
            accountLock.enableAccount(token.get());
            tokenService.deleteVerificationLink(token.get());

            terminal.success(ResponseType.VERIFICATION_SUCCESS);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(
                            new ResponseClient(ResponseType.VERIFICATION_SUCCESS,
                                    "SUCCESS: Verification complete"));
        } else {
            terminal.status(ResponseType.LINK_EXPIRED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            new ResponseClient(ResponseType.VERIFICATION_ERROR,
                                    "FAILED: Verification fail. Token is invalid or expired"));
        }
    }
}

