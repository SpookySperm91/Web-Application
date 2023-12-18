package john.server.repository_entity;

import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserCustomRepository {
    Optional<UserEntity> findByEmail(String email);

    UserEntity saveUserAccount(String username,
                         String hashedPassword, String salt,
                         String email,
                         LocalDateTime accountDateCreated);
}
