package john.server.common.components.interfaces;

import john.server.common.dto.ResponseLayer;
import org.springframework.stereotype.Component;

@Component
public interface PasswordStrength {
    ResponseLayer checkPassword(String password);
}
