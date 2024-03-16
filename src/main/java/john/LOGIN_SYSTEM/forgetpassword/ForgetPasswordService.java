package john.LOGIN_SYSTEM.forgetpassword;

import jakarta.servlet.http.HttpSession;
import john.LOGIN_SYSTEM.common.components.PasswordStrength;
import john.LOGIN_SYSTEM.common.components.VerificationCode;
import john.LOGIN_SYSTEM.common.components.email.EmailService;
import john.LOGIN_SYSTEM.common.components.email.TransactionType;
import john.LOGIN_SYSTEM.common.response.ResponseLayer;
import john.LOGIN_SYSTEM.common.response.ResponseTerminal;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.forgetpassword.token.CodeToken;
import john.LOGIN_SYSTEM.forgetpassword.token.CodeTokenService;
import john.LOGIN_SYSTEM.repository.entity.user.UserEntity;
import john.LOGIN_SYSTEM.repository.entity.user.UserRepository;
import john.LOGIN_SYSTEM.session.SessionService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
class ForgetPasswordService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordStrength passwordStrength;
    private final VerificationCode verification;
    private final EmailService emailService;
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
        this.emailService = email;
        this.terminal = terminal;
        this.redisSession = redisSession;
        this.tokenService = tokenService;
    }


    // VERIFY USER ACCOUNT IF EXIST
    // Check input formats
    // Check provided email if account exist
    // Proceed to next method for password match
    public ResponseLayer verifyAccountFirst(String email, HttpSession session) {
        var userEmail = userRepository.findByEmail(email);

        if (userEmail.isEmpty()) {
            return new ResponseLayer(
                    false, "Invalid email", HttpStatus.NOT_FOUND);
        }

        if (!userEmail.get().isEnabled()) {
            return new ResponseLayer(
                    false, "Account is locked", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = userEmail.get();
        CodeToken token = verificationCode(user);
        // send verification code via email
        emailService.sendEmail(user.getUsername(), user.getEmail(), token.getVerificationCode(), TransactionType.RESET_PASSWORD);

        // generate entry session for reset password
        int EXPIRATION_IN_MINUTES = 5;
        redisSession.setSession(session, "verification-code", token.getVerificationCode(), EXPIRATION_IN_MINUTES);
        redisSession.setSession(session, "user-id", user.getId().toString());

        terminal.status(ResponseType.ACCOUNT_EXIST);
        return new ResponseLayer(true, "Account exist", HttpStatus.OK);
    }

    // Generate verification code
    public CodeToken verificationCode(UserEntity user) {
        CodeToken token = new CodeToken(user.getId());
        verification.generateVerificationCode(token);
        return token;
    }


    // VERIFICATION PROCESS
    public ResponseLayer matchVerification(String userInput, HttpSession session) {
        String verificationCode = (String) redisSession.getSession(session, "verification-code");
        CodeToken verification = tokenService.handleExpiration(verificationCode);

        // Expired. remove session attributes
        if (verification == null) {
            redisSession.removeSession(session, "verification-code");
            redisSession.removeSession(session, "user-id");
            return new ResponseLayer(false, "Verification code expired", HttpStatus.BAD_REQUEST);
        }
        // Invalid input
        if (!userInput.equals(verification.getVerificationCode())) {
            return new ResponseLayer(false, "Invalid verification code", HttpStatus.BAD_REQUEST);
        }

        tokenService.deleteVerificationCode(verification);
        return new ResponseLayer(true, "Verified. Proceed to change-password", HttpStatus.OK);
    }


    // VALIDATE USER INPUTS FIRST BEFORE RESET PASSWORD
    // Check password strength
    // Check password if same as previous
    // Save new password; Return false if SYSTEM error persist
    public ResponseLayer resetPassword(ObjectId userId, String newPassword) {
        // Password strength
        // Retrieve account from database
        var checkNewPassword = passwordStrength.checkPassword(newPassword);
        Optional <UserEntity> checkUser = userRepository.findById(userId);

        if (!checkNewPassword.isSuccess()) {
            return checkNewPassword;
        } else if (checkUser.isEmpty()) {
            return new ResponseLayer(false, "User not found", HttpStatus.NOT_FOUND);
        }

        // Instantiate user account into variable
        UserEntity user = checkUser.get();

        // Return false if new password is same as the previous one
        if (passwordEncoder.matches(user.getSalt() + newPassword, user.getPassword())) {
            return new ResponseLayer(
                    false, "Provided password is the same as previous", HttpStatus.BAD_REQUEST);
        }

        // Perform password hashing before saving
        String hashedPassword = passwordEncoder.encode(user.getSalt() + newPassword);

        // Update user password
        if (userRepository.updatePassword(user, hashedPassword).isPresent()) {
            return new ResponseLayer(
                    true, "Password reset successfully", HttpStatus.OK);
        } else {
            return new ResponseLayer(
                    false, "Password reset fail", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
