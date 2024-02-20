package john.server.forgetpassword;

import john.server.common.components.PasswordComparison;
import john.server.common.components.PasswordStrength;
import john.server.common.components.VerificationCode;
import john.server.common.components.email.EmailService;
import john.server.common.components.email.TransactionType;
import john.server.common.response.ResponseLayer;
import john.server.common.response.ResponseTerminal;
import john.server.common.response.ResponseType;
import john.server.forgetpassword.token.CodeToken;
import john.server.repository.entity.user.UserEntity;
import john.server.repository.entity.user.UserRepository;
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
    private final VerificationCode verification;
    private final EmailService email;
    private final ResponseTerminal terminal;


    @Autowired
    public ForgetPasswordService(UserRepository userRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 PasswordComparison passwordComparison,
                                 PasswordStrength passwordStrength, VerificationCode verification, EmailService email, ResponseTerminal terminal) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordComparison = passwordComparison;
        this.passwordStrength = passwordStrength;
        this.verification = verification;
        this.email = email;
        this.terminal = terminal;
    }


    // VERIFY USER ACCOUNT IF EXIST
    // Check input formats
    // Check provided email if account exist
    // Proceed to next method for password match
    public ResponseLayer verifyAccountFirst(String email) {
        Optional<UserEntity> checkUserEmail = userRepository.findByEmail(email);

        if (checkUserEmail.isEmpty()) {
            return new ResponseLayer(
                    false, "Invalid email", HttpStatus.NOT_FOUND);
        }

        if (!checkUserEmail.get().isEnabled()) {
            return new ResponseLayer(
                    false, "Account is locked", HttpStatus.BAD_REQUEST);
        }
        verificationCode(checkUserEmail.get());
        terminal.status(ResponseType.ACCOUNT_EXIST);
        return new ResponseLayer(true, "Account exist", checkUserEmail.get(), HttpStatus.CONTINUE);
    }

    // VERIFICATION CODE PROCESS
    // Generate verification code
    // Send via user's email
    private void verificationCode(UserEntity user) {
        CodeToken token = new CodeToken(user.getId());
        verification.generateVerificationCode(token);

        email.sendEmail(user.getUsername(), user.getEmail(),
                token.getVerificationCode(), TransactionType.RESET_PASSWORD);
    }
































    // CHECK PASSWORD IF MATCHED
    // Validate provided password against stored user password
    // Returns true with user account INSTANTIATED; false otherwise
    public ResponseLayer checkPassword(UserEntity user, String providedPassword) {
        if (!passwordComparison.isPasswordValid(user, providedPassword)) {
            return new ResponseLayer(
                    false, "Invalid password", HttpStatus.BAD_REQUEST);
        }
        return new ResponseLayer(true, user);
    }

    public void sentVerificationCode() {

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
                    false, "Provided password is the same as previous", HttpStatus.BAD_REQUEST);
        }

        // Perform password hashing before saving
        String saltedPassword = user.getSalt() + newPassword;
        String hashedPassword = passwordEncoder.encode(saltedPassword);

        Optional<UserEntity> passwordSaved = userRepository.updatePassword(user, hashedPassword);

        if (passwordSaved.isPresent()) {
            return new ResponseLayer(
                    true, "Password reset successfully", HttpStatus.OK);
        }
        return new ResponseLayer(
                false, "Password reset fail", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
