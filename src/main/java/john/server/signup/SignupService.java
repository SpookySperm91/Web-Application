package john.server.signup;

import john.server.common.dto.UserDTO;
import john.server.common.dto.CheckUserInput;
import john.server.repository_entity.UserEntity;
import john.server.repository_entity.UserRepository;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
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

    public CheckUserInput checkUsername(String username) {
        if (username == null || username.isEmpty()) {
            return new CheckUserInput(false, "Username is empty");
        }
        if (username.length() > 20) {
            return new CheckUserInput(false, "Username exceeds maximum characters");
        }

        return new CheckUserInput(true);
    }


    public Optional<UserEntity> signupNewAccount(UserDTO request) {
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
            UserEntity savedUser = userRepository.saveUserAccount
                    (request.getUsername(),
                            hashedPassword, salt,
                            request.getEmail(),
                            DateCreated);
            return Optional.of(savedUser);


        } catch (Exception e) {
            // Handle any unexpected errors during account creation
            return Optional.empty();
        }
    }
}
