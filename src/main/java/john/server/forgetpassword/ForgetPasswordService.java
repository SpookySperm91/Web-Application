package john.server.forgetpassword;

import john.server.common.dto.CheckUserInput;
import john.server.repository_entity.UserEntity;
import john.server.repository_entity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Optional;


@Service
public class ForgetPasswordService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Autowired
    public ForgetPasswordService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Verify user account if exist
    public CheckUserInput verifyAccountFirst(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return new CheckUserInput(false, "Empty email and/or password");
        }

        Optional<UserEntity> checkUserEmail = userRepository.findByEmail(email);
        if (checkUserEmail.isEmpty()) {
            return new CheckUserInput(false, "Invalid email");
        }
        // Check if password matched
        return checkPassword(checkUserEmail.get(), password);
    }

    // Validate provided password against stored user password
    // Returns true with user account instantiated
    public CheckUserInput checkPassword(@NotNull UserEntity user, String providedPassword) {
        if (!isPasswordValid(user, providedPassword)) {
            return new CheckUserInput(false, "Invalid password");
        }
        // Password is valid; instantiate user account
        return new CheckUserInput(true, user);
    }
    private boolean isPasswordValid(UserEntity user, String providedPassword) {
        String saltedPassword = user.getSalt() + providedPassword;
        return passwordEncoder.matches(saltedPassword, user.getPassword());
    }

    // Reset password after validation
    public CheckUserInput resetPassword(@NotNull UserEntity user, String newPassword) throws Exception {
        if (newPassword.isEmpty()) {
            return new CheckUserInput(false, "Empty password");
        }

        try {
            String saltedPassword = user.getSalt() + newPassword;
            String hashedPassword = passwordEncoder.encode(saltedPassword);

            Optional<UserEntity> updatedUser = userRepository.updatePassword(user, hashedPassword);

            if (updatedUser.isPresent()) {
                return new CheckUserInput(true, "Password reset successfully");
            }
            return new CheckUserInput(false, "Password reset fail");

        } catch (Exception e) {
            throw new Exception("Error resetting password", e);
        }
    }
}
