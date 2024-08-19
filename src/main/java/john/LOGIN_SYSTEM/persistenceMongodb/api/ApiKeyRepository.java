package john.LOGIN_SYSTEM.persistenceMongodb.api;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends MongoRepository<ApiKey, ObjectId> {
    Optional<ApiKey> findApiKeyByKey(String key);
    Optional<ApiKey> deleteApiKeyByKey(String key);

}