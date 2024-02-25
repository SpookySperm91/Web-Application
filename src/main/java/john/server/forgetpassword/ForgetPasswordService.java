package john.server.forgetpassword;

import jakarta.servlet.http.HttpSession;
import john.server.common.components.PasswordStrength;
import john.server.common.components.VerificationCode;
import john.server.common.components.email.EmailService;
import john.server.common.components.email.TransactionType;
import john.server.common.response.ResponseLayer;
import john.server.common.response.ResponseTerminal;
import john.server.common.response.ResponseType;
import john.server.forgetpassword.token.CodeToken;
import john.server.forgetpassword.token.CodeTokenService;
import john.server.repository.entity.user.UserEntity;
import john.server.repository.entity.user.UserRepository;
import john.server.session.SessionService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class ForgetPasswordService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordStrength passwordStrength;
    private final VerificationCode verification;
    private final EmailService email;
    private final ResponseTerminal terminal;
    private final SessionService redisSession;
    private final CodeTokenService tokenService;

    @Autowired
    public ForgetPasswordService(UserRepository userRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 PasswordStrength passwordStrength,
                                 VerificationCode verification,
                                 EmailService email,
                                 ResponseTerminal terminal,
                                 SessionService redisSession,
                                 CodeTokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordStrength = passwordStrength;
        this.verification = verification;
        this.email = email;
        this.terminal = terminal;
        this.redisSession = redisSession;
        this.tokenService = tokenService;
    }


    // VERIFY USER ACCOUNT IF EXIST
    // Check input formats
    // Check provided email if account exist
    // Proceed to next method for password match
    public ResponseLayer verifyAccountFirst(String email, HttpSession session) {
        Optional<UserEntity> userEmail = userRepository.findByEmail(email);

        if (userEmail.isEmpty()) {
            return new ResponseLayer(
                    false, "Invalid email", HttpStatus.NOT_FOUND);
        }

        if (!userEmail.get().isEnabled()) {
            return new ResponseLayer(
                    false, "Account is locked", HttpStatus.BAD_REQUEST);
        }

        redisSession.setSession(session, "verification-code", verificationCode(userEmail.get()));
        redisSession.setSession(session, "user-id", userEmail.get().getId());

        terminal.status(ResponseType.ACCOUNT_EXIST);
        return new ResponseLayer(true, "Account exist", HttpStatus.CONTINUE);
    }

    // Generate verification code
    // Send via user's email
    private CodeToken verificationCode(UserEntity user) {
        CodeToken token = new CodeToken(user.getId());
        verification.generateVerificationCode(token);
        email.sendEmail(user.getUsername(),
                user.getEmail(),
                token.getVerificationCode(),
                TransactionType.RESET_PASSWORD);
        return token;
    }


    // VERIFICATION PROCESS
    public ResponseLayer matchVerification(String userInput, HttpSession session) {
        Object sessionObject = redisSession.getSession(session, "verification-code");

        if (sessionObject instanceof CodeToken) {
            CodeToken verification = tokenService.handleExpiration((CodeToken) sessionObject);

            // Expired. remove session attributes
            if(verification == null) {
                redisSession.removeSession(session, "verification-code");
                redisSession.removeSession(session, "user-id");
                return new ResponseLayer(false, "Verification code expired", HttpStatus.BAD_REQUEST);
            }
            // Invalid input
            if (!userInput.equals(verification.getVerificationCode())) {
                return new ResponseLayer(false, "Invalid verification code", HttpStatus.BAD_REQUEST);
            }

            return new ResponseLayer(true, "Verified. Proceed to change-password", HttpStatus.OK);
        } else {
            // Return System-error exception
            return new ResponseLayer(false, "Session error persist", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // VALIDATE USER INPUTS FIRST BEFORE RESET PASSWORD
    // Check password strength
    // Save new password; Return false if SYSTEM error persist
    public ResponseLayer resetPassword(ObjectId userId, String newPassword) {
        ResponseLayer checkNewPassword = passwordStrength.checkPassword(newPassword);
        if (!checkNewPassword.isSuccess()) {
            return checkNewPassword;
        }

        Optional <UserEntity> checkUser = userRepository.findById(userId);
        if (checkUser.isEmpty()) {
            return new ResponseLayer(false, "User not found", HttpStatus.NOT_FOUND);
        }
        UserEntity user = checkUser.get();

        // Perform password hashing before saving
        String hashedPassword = passwordEncoder.encode(user.getSalt() + newPassword);

        // Check if the new password is the same as the previous one
        if (user.getPassword().equals(hashedPassword)) {
            return new ResponseLayer(
                    false, "Provided password is the same as previous", HttpStatus.BAD_REQUEST);
        }

        // Update user password
        Optional<UserEntity> passwordSaved = userRepository.updatePassword(user, hashedPassword);
        if (passwordSaved.isPresent()) {
            return new ResponseLayer(
                    true, "Password reset successfully", HttpStatus.OK);
        }
        return new ResponseLayer(
                false, "Password reset fail", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
