package john.LOGIN_SYSTEM.register.token;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface LinkTokenRepository
        extends MongoRepository<LinkToken, ObjectId> {
    // @Cacheable(value = "verification-link", key = "#token")
    Optional<LinkToken> findByToken(String token);
}
