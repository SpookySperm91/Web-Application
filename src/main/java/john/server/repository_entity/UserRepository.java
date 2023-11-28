package john.server.repository_entity;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository
        extends MongoRepository<UserEntity, String>,
        checkEmail,
        saveNewUserAccount{
}


interface checkEmail {
    Optional<UserEntity> findEmail(String email);
}

interface saveNewUserAccount {
    void saveNewAccount(String username,
                        String hashedPassword, String salt,
                        String email,
                        LocalDateTime accountDateCreated);
}