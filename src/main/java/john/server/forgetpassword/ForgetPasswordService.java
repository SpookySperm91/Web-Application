package john.server.forgetpassword;

import john.server.common.components.interfaces.PasswordComparison;
import john.server.common.components.interfaces.PasswordStrength;
import john.server.common.dto.ResponseLayer;
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
    private final PasswordComparison passwordComparison;
    private final PasswordStrength passwordStrength;


    @Autowired
    public ForgetPasswordService(UserRepository userRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 PasswordComparison passwordComparison,
                                 PasswordStrength passwordStrength) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordComparison = passwordComparison;
        this.passwordStrength = passwordStrength;
    }


    // VERIFY USER ACCOUNT IF EXIST
    // Check input formats
    // Check provided email if account exist
    // Proceed to next method for password match
    public ResponseLayer verifyAccountFirst(String email, String password) {
        if (password.isEmpty()) {
            return new ResponseLayer(
                    false,
                    "Empty password",
                    HttpStatus.LENGTH_REQUIRED);
        }

        Optional<UserEntity> checkUserEmail = userRepository.findByEmail(email);

        if (checkUserEmail.isEmpty()) {
            return new ResponseLayer(
                    false,
                    "Invalid email",
                    HttpStatus.BAD_REQUEST);
        }
        return checkPassword(checkUserEmail.get(), password);
    }


    // CHECK PASSWORD IF MATCHED
    // Validate provided password against stored user password
    // Returns true with user account INSTANTIATED; false otherwise
    public ResponseLayer checkPassword(UserEntity user, String providedPassword) {
        if (!passwordComparison.isPasswordValid(user, providedPassword)) {
            return new ResponseLayer(
                    false,
                    "Invalid password",
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseLayer(true, user);
    }


    // VALIDATE USER INPUTS FIRST BEFORE RESET PASSWORD
    // Check new provided password strength first or if same as previous
    // Save new password; Return false if SYSTEM error persist
    public ResponseLayer resetPassword(UserEntity user, String newPassword) {
        ResponseLayer checkNewPassword = passwordStrength.checkPassword(newPassword);

        if (!checkNewPassword.isSuccess()) {
            return checkNewPassword;
        }
        if (passwordComparison.isPasswordValid(user, newPassword)) {
            return new ResponseLayer(
                    false,
                    "Provided password is the same as previous",
                    HttpStatus.BAD_REQUEST);
        }

        // Perform password hashing before saving
        String saltedPassword = user.getSalt() + newPassword;
        String hashedPassword = passwordEncoder.encode(saltedPassword);

        Optional<UserEntity> passwordSaved = userRepository.updatePassword(user, hashedPassword);

        if (passwordSaved.isPresent()) {
            return new ResponseLayer(
                    true,
                    "Password reset successfully",
                    HttpStatus.OK);
        }
        return new ResponseLayer(
                false,
                "Password reset fail",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
