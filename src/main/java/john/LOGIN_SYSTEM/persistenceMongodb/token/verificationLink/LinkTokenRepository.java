package john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkTokenRepository
        extends MongoRepository<LinkToken, ObjectId> {
    // @Cacheable(value = "verification-link", key = "#token")
    Optional<LinkToken> findByToken(String token);
}
