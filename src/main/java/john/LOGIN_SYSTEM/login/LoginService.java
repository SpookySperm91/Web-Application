package john.LOGIN_SYSTEM.login;

import john.LOGIN_SYSTEM.common.components.PasswordComparison;
import john.LOGIN_SYSTEM.common.response.ResponseLayer;
import john.LOGIN_SYSTEM.repository.entity.user.UserEntity;
import john.LOGIN_SYSTEM.repository.entity.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class LoginService {
    private final UserRepository userRepository;
    private final PasswordComparison passwordComparison;

    public LoginService(UserRepository userRepository, PasswordComparison passwordComparison) {
        this.userRepository = userRepository;
        this.passwordComparison = passwordComparison;
    }


    // AUTHENTICATE USER ACCOUNT; Return false if account didn't exist
    // Perform secure password comparison; Return a response
    public ResponseLayer authenticateUser(String email, String password) {
        Optional<UserEntity> userExist = userRepository.findByEmail(email);

        if (userExist.isEmpty()) {
            return new ResponseLayer(false, "Invalid Email or Password", HttpStatus.BAD_REQUEST);
        }

        // Wrong password
        if (!passwordComparison.isPasswordValid(userExist.get(), password)) {
            return new ResponseLayer(false, "Invalid Email or Password", HttpStatus.BAD_REQUEST);
        }

        // Account Locked
        if(!userExist.get().isEnabled()) {
            return new ResponseLayer(false, "Account is Locked", HttpStatus.BAD_REQUEST);
        }

        // Set account login true
        userExist.get().setLogged(true);
        userRepository.save(userExist.get());
        return new ResponseLayer(true, "Login Success", HttpStatus.OK);
    }
}

