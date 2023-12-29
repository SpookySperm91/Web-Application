package john.server.login;

import john.server.common.components.interfaces.PasswordComparison;
import john.server.common.dto.ResponseLayer;
import john.server.repository_entity.UserEntity;
import john.server.repository_entity.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
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
        return new ResponseLayer(true, "Login Success", HttpStatus.OK);
    }
}

