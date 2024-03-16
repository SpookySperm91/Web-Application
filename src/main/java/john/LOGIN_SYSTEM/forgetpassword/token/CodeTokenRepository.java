package john.LOGIN_SYSTEM.forgetpassword.token;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CodeTokenRepository
        extends MongoRepository<CodeToken, ObjectId> {
    // @Cacheable(value = "verification-code", key = "#code")
    Optional<CodeToken> findByVerificationCode (String code);
}
