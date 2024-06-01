package john.LOGIN_SYSTEM.monolith.register;

import john.LOGIN_SYSTEM.common.components.AccountLock;
import john.LOGIN_SYSTEM.common.components.PasswordStrength;
import john.LOGIN_SYSTEM.common.dto.UserDTO;
import john.LOGIN_SYSTEM.common.response.*;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink.LinkTokenService;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/register")
class RegisterController {
    private final RegisterService serviceLayer;
    private final PasswordStrength passwordStrength;
    private final LinkTokenService tokenService;
    private final AccountLock accountLock;

    @Autowired
    public RegisterController(RegisterService signupService,
                              PasswordStrength passwordStrength,
                              LinkTokenService tokenService,
                              AccountLock accountLock) {
        this.serviceLayer = signupService;
        this.passwordStrength = passwordStrength;
        this.tokenService = tokenService;
        this.accountLock = accountLock;
    }


    // CHECK USER'S INPUTS BEFORE SIGNUP
    // Sanitize user inputs from malicious attempt
    // Check email and username format; Return bad response if error occurs
    // Proceed to create new account
    // Return response
    @PostMapping("/")
        public ResponseEntity<ResponseClient> signupUser(@Valid @RequestBody UserDTO request) {
        String sanitizedEmail = Encode.forHtml(request.getEmail());
        String sanitizedUsername = Encode.forHtml(request.getUsername());
        String sanitizedPassword = Encode.forHtml(request.getPassword());

        ResponseLayer email = serviceLayer.checkEmail(sanitizedEmail);
        ResponseLayer username = serviceLayer.checkUsername(sanitizedUsername);
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
        var signupResponse = serviceLayer.verificationProcess(sanitizedUsername, sanitizedEmail, sanitizedPassword);

        return ResponseEntity.status(signupResponse.getHttpStatus())
                .body(new ResponseClient(
                                ResponseType.SIGNUP_PENDING,
                                signupResponse.getMessage()));
    }


    // VERIFY ACCOUNT
    // Check if link is expire or valid
    // Enable locked account
    // Delete the link
    @GetMapping("/{verification}")
    public ResponseEntity<String> verificationProcess(@PathVariable String verification) {

        return tokenService.getVerificationLink(verification)
                .map(token -> {
                    accountLock.enableAccount(token);
                    tokenService.deleteVerificationLink(token);

                    return ResponseEntity.accepted()
                            .body(readHTMLFile("static/verification-success.html"));
                }).orElseGet(() ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(readHTMLFile("static/verification-expired.html")));

    }

    private String readHTMLFile(String fileName) {
        ClassPathResource resource = new ClassPathResource(fileName);
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(resource.getURI()));
            return new String(bytes);
        } catch (IOException e) {
            System.out.println("Error reading HTML file: " + e.getMessage());
            return ""; // Return empty string if file reading fails
        }
    }
}

