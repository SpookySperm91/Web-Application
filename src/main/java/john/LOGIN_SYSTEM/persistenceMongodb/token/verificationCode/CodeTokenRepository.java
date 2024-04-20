package john.LOGIN_SYSTEM.persistenceMongodb.token.verificationCode;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeTokenRepository
        extends MongoRepository<CodeToken, ObjectId> {
    // @Cacheable(value = "verification-code", key = "#code")
    Optional<CodeToken> findByVerificationCode (String code);
}
