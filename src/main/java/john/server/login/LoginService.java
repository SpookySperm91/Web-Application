package john.server.login;

import john.server.common.dto.CheckUserInput;
import john.server.repository_entity.UserEntity;
import john.server.repository_entity.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // AUTHENTICATE USER ACCOUNT; Return false if account didn't exist
    // Perform secure password comparison; Return a response
    public CheckUserInput authenticateUser(String email, String password) {
        Optional<UserEntity> userExist = userRepository.findByEmail(email);

        if (userExist.isEmpty()) {
            return new CheckUserInput(false, "Invalid Email or Password", HttpStatus.BAD_REQUEST);
        }

        if (isPasswordValid(userExist.get(), password)) {
            return new CheckUserInput(true, "Login Success", HttpStatus.OK);
        }
        return new CheckUserInput(false, "Invalid Email or Password", HttpStatus.BAD_REQUEST);
    }
    // Perform secure password comparison
    private boolean isPasswordValid(UserEntity user, String providedPassword) {
        // salt + provided password
        String saltedPassword = user.getSalt() + providedPassword;
        return passwordEncoder.matches(saltedPassword, user.getPassword());
    }
}

