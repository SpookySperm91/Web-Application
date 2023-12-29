package john.server.repository_entity;

import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserCustomRepository {
    Optional<UserEntity> findByEmail(String email);

    void saveUserAccount(String username,
                         String hashedPassword, String salt,
                         String email,
                         LocalDateTime accountDateCreated);

    Optional<UserEntity> updatePassword(UserEntity user, String password);
}
