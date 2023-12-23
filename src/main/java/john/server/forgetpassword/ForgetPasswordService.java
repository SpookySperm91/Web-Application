package john.server.forgetpassword;

import john.server.common.dto.CheckUserInput;
import john.server.repository_entity.UserEntity;
import john.server.repository_entity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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


    // VERIFY USER ACCOUNT IF EXIST
    // Check input formats
    // Check provided email if account exist
    // Check if provided password matched
    public CheckUserInput verifyAccountFirst(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return new CheckUserInput(
                    false,
                    "Empty email and / or password",
                    HttpStatus.LENGTH_REQUIRED);
        }

        Optional<UserEntity> checkUserEmail = userRepository.findByEmail(email);

        if (checkUserEmail.isEmpty()) {
            return new CheckUserInput(
                    false,
                    "Invalid email",
                    HttpStatus.BAD_REQUEST);
        }
        return checkPassword(checkUserEmail.get(), password);
    }


    // CHECK PASSWORD IF MATCHED
    // Validate provided password against stored user password
    // Returns true with user account instantiated
    public CheckUserInput checkPassword(UserEntity user, String providedPassword) {
        if (!isPasswordValid(user, providedPassword)) {
            return new CheckUserInput(
                    false,
                    "Invalid password",
                    HttpStatus.BAD_REQUEST);
        }
        return new CheckUserInput(true, user);
    }
    private boolean isPasswordValid(UserEntity user, String providedPassword) {
        String saltedPassword = user.getSalt() + providedPassword;
        return passwordEncoder.matches(saltedPassword, user.getPassword());
    }


    // VALIDATE USER INPUTS FIRST BEFORE RESET PASSWORD
    // Check new provided password first if not empty
    // Save new password; Return false if SYSTEM error persist
    public CheckUserInput resetPassword(UserEntity user, String newPassword) {
        if (newPassword.isEmpty()) {
            return new CheckUserInput(
                    false,
                    "Empty password",
                    HttpStatus.LENGTH_REQUIRED);
        }

            String saltedPassword = user.getSalt() + newPassword;
            String hashedPassword = passwordEncoder.encode(saltedPassword);

            Optional<UserEntity> updatedUser = userRepository.updatePassword(user, hashedPassword);

            if (updatedUser.isPresent()) {
                return new CheckUserInput(
                        true,
                        "Password reset successfully",
                        HttpStatus.OK);
            }
            return new CheckUserInput(
                    false,
                    "Password reset fail",
                    HttpStatus.SERVICE_UNAVAILABLE);
    }
}
