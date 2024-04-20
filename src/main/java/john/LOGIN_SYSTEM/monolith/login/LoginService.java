package john.LOGIN_SYSTEM.monolith.login;

import john.LOGIN_SYSTEM.common.components.PasswordComparison;
import john.LOGIN_SYSTEM.common.response.ResponseLayer;
import john.LOGIN_SYSTEM.persistenceMongodb.user.UserEntity;
import john.LOGIN_SYSTEM.persistenceMongodb.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class LoginService {
    private final UserRepository repository;
    private final PasswordComparison passwordComparison;

    public LoginService(UserRepository repository, PasswordComparison passwordComparison) {
        this.repository = repository;
        this.passwordComparison = passwordComparison;
    }


    // AUTHENTICATE USER ACCOUNT; Return false if account didn't exist
    // Perform secure password comparison; Return a response
    public ResponseLayer authenticateUser(String email, String password) {
        Optional<UserEntity> userExist = repository.findByEmail(email);

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
        repository.save(userExist.get());
        return new ResponseLayer(true, "Login Success", HttpStatus.OK);
    }
}

