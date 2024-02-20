package john.server.common.components;

import john.server.repository.entity.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;


// PERFORM SECURE PASSWORD COMPARISON
@Component
public class PasswordComparison {
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public PasswordComparison(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isPasswordValid(UserEntity user, String providedPassword) {
        if (providedPassword.isEmpty()) {
            return false;
        }
        // salt + provided password
        String saltedPassword = user.getSalt() + providedPassword;
        return passwordEncoder.matches(saltedPassword, user.getPassword());
    }
}
