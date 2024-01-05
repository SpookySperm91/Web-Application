package john.server.register;

import john.server.common.dto.DTOUser;
import john.server.common.dto.ResponseLayer;
import john.server.repository_entity.UserRepository;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegisterService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public RegisterService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // CHECK PROVIDED USERNAME; Return response to the Controller
    public ResponseLayer checkUsername(String username) {
        if (username == null || username.isEmpty()) {
            return new ResponseLayer(false, "Username is empty");
        }
        if (username.length() > 20) {
            return new ResponseLayer(false, "Username exceeds maximum characters");
        }

        return new ResponseLayer(true);
    }


    // CHECK PROVIDED EMAIL; Return response to the Controller
    public ResponseLayer checkEmail(String email) {
        if (email == null || email.isEmpty()) {
            return new ResponseLayer(false, "Email is empty");
        }
        if (!EmailValidator.getInstance().isValid(email)) {
            return new ResponseLayer(false, "Invalid email format");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return new ResponseLayer(false, "Account already exists");
        }

        return new ResponseLayer(true);
    }


    // REGISTER NEW ACCOUNT
    // Hash the provided password first before saving

    // -- Password Hashing Procedure -- //
    // Generate a unique salt
    // Combine the salt and password (hashedPassword = salt + provided password)
    // Password is now hashed
    // ---------------------- //

    // Set Date for creation
    // Save the new account
    // Return response
    public ResponseLayer signupNewAccount(String username, String email, String password) {
        try {
            String salt = BCrypt.gensalt(); // Generate a unique salt

            String saltedPassword = salt + password;
            String hashedPassword = passwordEncoder.encode(saltedPassword);

            LocalDateTime DateCreated = LocalDateTime.now();

            userRepository.saveUserAccount
                            (username,
                            hashedPassword, salt,
                            email,
                            DateCreated);
            return new ResponseLayer(true,
                    "Registration successful",
                    HttpStatus.OK);


        } catch (Exception e) {
            return new ResponseLayer(false,
                    "Failed to create an account",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
