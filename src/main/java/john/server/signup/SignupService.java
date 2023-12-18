package john.server.signup;

import john.server.repository_entity.UserEntity;
import john.server.repository_entity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.validator.routines.EmailValidator;
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


    public Optional<UserEntity> checkEmailExistFirst(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            // Handle invalid email format
            throw new IllegalArgumentException("Invalid email format");
        }
        // Check if given email exists
        return userRepository.findByEmail(email);
    }


    public Optional<UserEntity> signupNewAccount(SignupDTO request) {
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
