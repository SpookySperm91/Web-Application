package john.LOGIN_SYSTEM.monolith.forgetpassword;

import jakarta.servlet.http.HttpSession;
import john.LOGIN_SYSTEM.common.components.PasswordStrength;
import john.LOGIN_SYSTEM.common.components.email.EmailService;
import john.LOGIN_SYSTEM.common.components.email.TransactionType;
import john.LOGIN_SYSTEM.common.dto.PasswordDTO;
import john.LOGIN_SYSTEM.common.exception.RedisConnectionException;
import john.LOGIN_SYSTEM.common.response.ResponseLayer;
import john.LOGIN_SYSTEM.common.response.ResponseTerminal;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationCode.CodeToken;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationCode.CodeTokenService;
import john.LOGIN_SYSTEM.persistenceMongodb.user.UserEntity;
import john.LOGIN_SYSTEM.persistenceMongodb.user.UserRepository;
import john.LOGIN_SYSTEM.session.SessionAttr;
import john.LOGIN_SYSTEM.session.SessionService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class ForgetPasswordService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordStrength passwordStrength;
    private final EmailService emailService;
    private final ResponseTerminal terminal;
    private final SessionService redisSession;
    private final CodeTokenService tokenService;

    @Autowired
    public ForgetPasswordService(UserRepository userRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 PasswordStrength passwordStrength,
                                 EmailService email,
                                 ResponseTerminal terminal,
                                 SessionService redisSession,
                                 CodeTokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordStrength = passwordStrength;
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
            return new ResponseLayer(false,
                    "Email does not Exist",
                    ResponseType.RESET_PASSWORD_ERROR,
                    HttpStatus.NOT_FOUND);
        }

        if (!userEmail.get().isEnabled()) {
            return new ResponseLayer(false,
                    "Account is locked",
                    ResponseType.RESET_PASSWORD_ERROR,
                    HttpStatus.BAD_REQUEST);
        }

        UserEntity user = userEmail.get();
        CodeToken token = verificationCode(user);
        tokenService.saveVerificationCode(token);

        // send verification code via email
        emailService.sendEmail(user.getUsername(),              // username
                user.getEmail(),                                // user's email
                token.getVerificationCode(),                    // body
                TransactionType.RESET_PASSWORD);                // transaction type

        // instantiating data for session back to the controller
        ResponseLayer.DataAccess data = new ResponseLayer.DataAccess();
        data.addString(token.getVerificationCode());
        data.addObjectId(user.getId());

        terminal.status(ResponseType.ACCOUNT_EXIST);
        return new ResponseLayer(data,
                true,
                "Account exist",
                ResponseType.RESET_PASSWORD_SUCCESS,
                HttpStatus.OK);
    }

    // Generate verification code.
    private CodeToken verificationCode(UserEntity user) {
        CodeToken token = new CodeToken(user.getId());
        token.setEmail(user.getEmail());
        tokenService.generateVerificationCode(token);
        return token;
    }


    // VERIFICATION PROCESS
    // Check session for verification code. Use database if session is down
    // Check verifcation if expired or not
    // Return response
    public ResponseLayer matchVerification(String userInput, String email, HttpSession session) {
        String verificationCode;
        CodeToken verification = null;

        // redis session extraction
        try {
            verificationCode = (String) redisSession.getSession(session, SessionAttr.VERIFICATION_CODE_SESSION);
            if (verificationCode != null) {
                verification = tokenService.handleExpiration(verificationCode);
            }
        } catch (RedisConnectionFailureException e) { // session offline? fallback to persistence
            CodeToken persistence = tokenService.getByEmail(email);
            verification = tokenService.handleExpiration(persistence.getVerificationCode());
        }

        // expired. remove session attributes
        if (verification == null) {
            try {
                redisSession.removeSession(session, SessionAttr.VERIFICATION_CODE_SESSION);  // Remove, attr = VerificationCode
                redisSession.removeSession(session, SessionAttr.USER_ID_SESSION);            // Remove, attr = UserId
            } catch (RedisConnectionFailureException e) {
                terminal.status(ResponseType.REDIS_SESSION_OFFLINE);
                throw new RedisConnectionException("Redis connection error: " + e.getMessage());
            }
            return new ResponseLayer(false,
                    "Verification code expired",
                    ResponseType.RESET_PASSWORD_ERROR,
                    HttpStatus.BAD_REQUEST);
        }

        // invalid input
        if (!userInput.equals(verification.getVerificationCode())) {
            return new ResponseLayer(false,
                    "Invalid verification code",
                    ResponseType.RESET_PASSWORD_ERROR,
                    HttpStatus.BAD_REQUEST);
        }

        tokenService.deleteVerificationCode(verification);
        return new ResponseLayer(true,
                "Verified. Proceed to change-password",
                ResponseType.RESET_PASSWORD_SUCCESS,
                HttpStatus.OK);
    }


    // VALIDATE USER INPUTS FIRST BEFORE RESET PASSWORD
    // Check password strength
    // Check password if same as previous
    // Save new password; Return false if SYSTEM error persist
    public ResponseLayer resetPassword(String userId, PasswordDTO password) {
        // Password strength
        // Retrieve account from database
        if (!password.getNewPassword().equals(password.getConfirmPassword())){
            return new ResponseLayer(false,
                    "Confirm password not same",
                    ResponseType.RESET_PASSWORD_ERROR,
                    HttpStatus.BAD_REQUEST);
        }


        var checkNewPassword = passwordStrength.checkPassword(password.getNewPassword());
        Optional<UserEntity> checkUser = userRepository.findById(new ObjectId(userId));

        if (!checkNewPassword.isSuccess()) {
            return checkNewPassword;
        } else if (checkUser.isEmpty()) {
            return new ResponseLayer(false,
                    "User not found",
                    ResponseType.RESET_PASSWORD_ERROR,
                    HttpStatus.NOT_FOUND);
        }

        // Instantiate user account into variable
        UserEntity user = checkUser.get();

        // Return false if new password is same as the previous one
        if (passwordEncoder.matches(user.getSalt() + password.getNewPassword(), user.getPassword())) {
            return new ResponseLayer(false,
                    "Provided password is the same as previous",
                    ResponseType.RESET_PASSWORD_ERROR,
                    HttpStatus.BAD_REQUEST);
        }

        // Perform password hashing before saving
        // Update user password
        String hashedPassword = passwordEncoder.encode(user.getSalt() + password.getNewPassword());
        if (userRepository.updatePassword(user, hashedPassword).isPresent()) {
            return new ResponseLayer(true,
                    "Password reset successfully",
                    ResponseType.RESET_PASSWORD_SUCCESS,
                    HttpStatus.OK);
        } else {
            return new ResponseLayer(false,
                    "Password reset fail",
                    ResponseType.RESET_PASSWORD_EXCEPTION,
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
