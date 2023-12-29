package john.server.common.components.interfaces;

import john.server.repository_entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public interface PasswordComparison {
     boolean isPasswordValid(UserEntity user, String providedPassword);
}
