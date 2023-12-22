package john.server.login;

import john.server.common.dto.CheckUserInput;
import john.server.repository_entity.UserEntity;
import john.server.repository_entity.UserRepository;
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

    public CheckUserInput authenticateUser(String email, String password) {
        Optional<UserEntity> userExist = userRepository.findByEmail(email);

        if (userExist.isEmpty()) {
            return new CheckUserInput(false, "Invalid Email or Password");
        }

        // Perform secure password comparison
        if (isPasswordValid(userExist.get(), password)) {
            return new CheckUserInput(true, "Login Success");
        }
        return new CheckUserInput(false, "Invalid Email or Password");
    }

    private boolean isPasswordValid(UserEntity user, String providedPassword) {
        // salt + provided password
        String saltedPassword = user.getSalt() + providedPassword;
        return passwordEncoder.matches(saltedPassword, user.getPassword());
    }
}

