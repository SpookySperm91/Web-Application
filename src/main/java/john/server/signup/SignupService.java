package john.server.signup;

import john.server.common.dto.CheckUserInput;
import john.server.common.dto.UserDTO;
import john.server.repository_entity.UserRepository;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SignupService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public SignupService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // CHECK PROVIDED EMAIL; Return response to the Controller
    public CheckUserInput checkEmail(String email) {
        if (email == null || email.isEmpty()) {
            return new CheckUserInput(false, "Email is empty");
        }
        if (!EmailValidator.getInstance().isValid(email)) {
            return new CheckUserInput(false, "Invalid email format");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return new CheckUserInput(false, "Account already exists");
        }

        return new CheckUserInput(true);
    }


    // CHECK PROVIDED USERNAME; Return response to the Controller
    public CheckUserInput checkUsername(String username) {
        if (username == null || username.isEmpty()) {
            return new CheckUserInput(false, "Username is empty");
        }
        if (username.length() > 20) {
            return new CheckUserInput(false, "Username exceeds maximum characters");
        }

        return new CheckUserInput(true);
    }


    // REGISTER NEW ACCOUNT
    // Hash the provided password first before saving

    // -- Password Hashing Procedure -- //
    // Generate a unique salt
    // Combine the salt and password (hashed-password = salt + provided password)
    // Hash the salted password

    // Set Date for creation
    // Save the new account
    // Return response
    public CheckUserInput signupNewAccount(UserDTO request) {
        try {
            String password = request.getPassword();
            String salt = BCrypt.gensalt(); // Generate a unique salt

            String saltedPassword = salt + password;
            String hashedPassword = passwordEncoder.encode(saltedPassword);

            LocalDateTime DateCreated = LocalDateTime.now();

            userRepository.saveUserAccount
                    (request.getUsername(),
                            hashedPassword, salt,
                            request.getEmail(),
                            DateCreated);
            return new CheckUserInput(true,
                    "Registration successful",
                    HttpStatus.OK);


        } catch (Exception e) {
            return new CheckUserInput(false,
                    "Failed to create an account",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
