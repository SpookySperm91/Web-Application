package john.LOGIN_SYSTEM.repository.entity.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends
        MongoRepository<UserEntity, ObjectId>,
        UserCustomRepository {
    Optional<UserEntity> findByEmail(String email);
}


