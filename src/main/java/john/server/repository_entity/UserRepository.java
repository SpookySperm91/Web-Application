package john.server.repository_entity;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends
        MongoRepository<UserEntity, ObjectId>,
        UserCustomRepository {
}


