package john.server.signup;

import john.server.repository_entity.UserEntity;
import john.server.repository_entity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SignupService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public SignupService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public ResponseEntity<String> checkEmailExistFirst(String email){
        // Check if given email exist
        Optional<UserEntity> emailExists = userRepository.findEmail(email);


        return emailExists.map(
                // Email does not exist, proceed to register
                userEntity
                        -> ResponseEntity.ok("DEFAULT RESPONSE: Email doesn't exist. Proceed to register"))
                // Email already exist
                .orElseGet(()
                        -> ResponseEntity.badRequest().body("DEFAULT RESPONSE: Error. Email already exist"));

    }



    public ResponseEntity<String> signupNewAccount(DTO request) {
        try {
            String password = request.getPassword();
            String salt = BCrypt.gensalt(); // Generate a unique salt

            // Combine the salt and password
            String saltedPassword = salt + password;

            // Hash the salted password
            String hashedPassword = passwordEncoder.encode(saltedPassword);

            // Set Date for creation
            LocalDateTime DateCreated = LocalDateTime.now();

            // Save the new account and handle the response
            userRepository.saveNewAccount
                    (request.getUsername(),
                            hashedPassword, salt,
                            request.getEmail(),
                            DateCreated);
            return ResponseEntity.ok("DEFAULT RESPONSE: Registration successful");


        } catch (Exception e) {
            // Handle any unexpected errors during account creation
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("DEFAULT RESPONSE: Error. Server failed to create account");
        }
    }

}
