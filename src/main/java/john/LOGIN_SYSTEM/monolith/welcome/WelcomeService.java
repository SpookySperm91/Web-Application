package john.LOGIN_SYSTEM.monolith.welcome;

import john.LOGIN_SYSTEM.common.components.PasswordComparison;
import john.LOGIN_SYSTEM.common.components.PasswordStrength;
import john.LOGIN_SYSTEM.common.components.email.EmailService;
import john.LOGIN_SYSTEM.common.components.email.TransactionType;
import john.LOGIN_SYSTEM.common.dto.PasswordDTO;
import john.LOGIN_SYSTEM.common.response.ResponseLayer;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.persistenceMongodb.token.VerificationType;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink.LinkToken;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink.LinkTokenService;
import john.LOGIN_SYSTEM.persistenceMongodb.user.UserEntity;
import john.LOGIN_SYSTEM.persistenceMongodb.user.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WelcomeService {
    private final PasswordComparison passwordComparison;
    private final PasswordStrength passwordStrength;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final LinkTokenService linkService;
    private final VerificationType verificationType = VerificationType.CHANGE_PASSWORD_VIA_EMAIL;

    @Autowired
    public WelcomeService(PasswordComparison passwordComparison,
                          PasswordStrength passwordStrength,
                          BCryptPasswordEncoder passwordEncoder,
                          UserRepository userRepository,
                          EmailService emailService,
                          LinkTokenService linkService) {
        this.passwordComparison = passwordComparison;
        this.passwordStrength = passwordStrength;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.linkService = linkService;
    }


    public ResponseLayer changePassword(String userId, PasswordDTO request) {
        Optional<UserEntity> userPersistence = userRepository.findById(new ObjectId(userId));
        List<String> errorMessages = new ArrayList<>();

        if (userPersistence.isPresent()) {
            UserEntity user = userPersistence.get();

            boolean isPreviousPasswordValid = passwordComparison.isPasswordValid(user, request.getPreviousPassword());
            boolean isNewPasswordSameAsOld = passwordComparison.isPasswordValid(user, request.getNewPassword());

            if (!isPreviousPasswordValid) {
                errorMessages.add("Previous password is incorrect!");
            }
            if (isNewPasswordSameAsOld) {
                errorMessages.add("New password is same as before. Try different!");
            }
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                errorMessages.add("Confirm password is not the same");
            }
            if (!passwordStrength.checkPassword(request.getNewPassword()).isSuccess()) {
                errorMessages.add(passwordStrength.checkPassword(request.getNewPassword()).getMessage());
            }
            if (!errorMessages.isEmpty()) {
                return new ResponseLayer(false,
                        String.join(", ", errorMessages),
                        ResponseType.SIGNUP_ERROR,
                        HttpStatus.BAD_REQUEST);
            }
            // Update password and save user
            userRepository.updatePassword(user,
                    passwordEncoder.encode(user.getSalt() + request.getNewPassword()));

            return new ResponseLayer(true,
                    "Password changed successfully!",
                    ResponseType.RESET_PASSWORD_SUCCESS,
                    HttpStatus.ACCEPTED);
        }

        // Handle case where user is not found
        return new ResponseLayer(false,
                "System problem: User data cannot be found.",
                ResponseType.RESET_PASSWORD_EXCEPTION,
                HttpStatus.BAD_REQUEST);
    }


    public ResponseLayer changePasswordByEmail(String id, String email) {
        Optional<UserEntity> getUser = userRepository.findByIdAndEmail(new ObjectId(id), email);
        if (getUser.isEmpty()) {
            return new ResponseLayer("Check email if verification is sent.",
                    ResponseType.VERIFICATION_SENT_UNKNOWN,
                    HttpStatus.OK);
        }
        UserEntity user = getUser.get();

        // generate token
        LinkToken token = new LinkToken(new ObjectId(id));
        linkService.generateToken(token);
        linkService.saveVerificationLink(token);

        // generate link and send via email
        String changePasswordLink = linkService.generateLink(token.getToken(), verificationType);
        emailService.sendEmail(
                user.getUsername(),                                   // username
                user.getEmail(),                                      // user's email
                changePasswordLink,                                   // body
                TransactionType.RESET_PASSWORD_AUTHENTICATED);        // email type

        // instantiating data for session back to the controller
        ResponseLayer.DataAccess data = new ResponseLayer.DataAccess();
        data.addObject(token);                                        // body = LinkToken
        data.addDate(token.getExpireAt());                            // expire

        return new ResponseLayer(data,
                "Check email if verification is sent",
                ResponseType.VERIFICATION_SENT_UNKNOWN,
                HttpStatus.OK);
    }
}
