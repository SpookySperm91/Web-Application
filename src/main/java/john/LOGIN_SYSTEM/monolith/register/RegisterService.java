package john.LOGIN_SYSTEM.monolith.register;

import jakarta.servlet.http.HttpSession;
import john.LOGIN_SYSTEM.common.components.email.EmailService;
import john.LOGIN_SYSTEM.common.components.email.TransactionType;
import john.LOGIN_SYSTEM.common.response.ResponseLayer;
import john.LOGIN_SYSTEM.common.response.ResponseTerminal;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.persistenceMongodb.token.VerificationType;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink.LinkToken;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink.LinkTokenService;
import john.LOGIN_SYSTEM.persistenceMongodb.user.UserEntity;
import john.LOGIN_SYSTEM.persistenceMongodb.user.UserRepository;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
class RegisterService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ResponseTerminal terminal;
    private final LinkTokenService linkService;


    @Autowired
    public RegisterService(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           EmailService emailService,
                           ResponseTerminal terminal,
                           LinkTokenService linkService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.terminal = terminal;
        this.linkService = linkService;
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
    // Generate pending user and verification token
    // Generate verification link and send the user email
    public ResponseLayer verificationProcess(String username, String email, String password) {
        // Generate pending user
        UserEntity pendingUser = userRepository.generateUserID();
        userRepository.save(pendingUser);  // Save the UserEntity to the database

        // Generate token for link url
        LinkToken tokenID = new LinkToken(pendingUser.getId());
        linkService.generateToken(tokenID);
        linkService.saveVerificationLink(tokenID);

        // Generate link. Send verification via email
        String link = linkService.generateLink(tokenID.getToken(), VerificationType.CREATE_USER);
        emailService.sendEmail(username, email, link, TransactionType.REGISTER);

        // save link id in session for faster retrieval
        ResponseLayer.DataAccess data = new ResponseLayer.DataAccess();
        data.addObject(tokenID);                                                    // body = LinkToken
        data.addDate(tokenID.getExpireAt());                                        // expire

        terminal.status(ResponseType.LINK_GENERATED);
        return savePendingAccount(data, pendingUser, username, email, password);
    }


    // SAVE USER ACCOUNT
    // Hash the provided password first before saving

    // -- Password Hashing Procedure -- //
    // Generate a unique salt
    // Combine the salt and password (hashedPassword = salt + provided password)
    // Password is now hashed
    // ---------------------- //

    // Set Date for creation
    // Save the new account (with the instance id)
    // Return response
    private ResponseLayer savePendingAccount(ResponseLayer.DataAccess data, UserEntity pendingUser, String username, String email, String password) {
        try {
            String salt = BCrypt.gensalt(); // Generate a unique salt
            String hashedPassword = passwordEncoder.encode(salt + password);
            LocalDateTime dateCreated = LocalDateTime.now();

            // Save data
            pendingUser.setUsername(username);
            pendingUser.setEmail(email);
            pendingUser.setPassword(hashedPassword);
            pendingUser.setSalt(salt);
            pendingUser.setAccountDateCreated(dateCreated);
            pendingUser.setEnabled(false); // Account is locked
            userRepository.saveUserAccount(pendingUser);

            terminal.success(ResponseType.SIGNUP_SUCCESS);
            return new ResponseLayer(data,
                    true,
                    "Registration successful. Proceed to email verification"
                    , HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseLayer(false,
                    "Failed to create an account",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


