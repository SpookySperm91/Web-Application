package john.server.repository_entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
@Repository
public class CustomRepository
        implements checkEmail, saveNewUserAccount{
    private final MongoTemplate mongoTemplate;
    @Autowired
    public CustomRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;

    }

    public Optional<UserEntity> findEmail(String email) {
        // Define the query criteria to find a document with the specified email
        Query query = new Query(Criteria.where("account.email").is(email));

        // Use the MongoTemplate to execute the query and get the result
        UserEntity result = mongoTemplate.findOne(query, UserEntity.class);

        // Convert the result to an Optional
        return Optional.ofNullable(result);
    }

    public void saveNewAccount(String username,
                               String hashedPassword, String salt,
                               String email,
                               LocalDateTime accountDateCreated) {
        // Create instance
        UserDataEntity userData =
                new UserDataEntity(username, hashedPassword, salt, email, accountDateCreated);

        // Save instance into UserEntity
        UserEntity newUser =
                new UserEntity(null, userData);

        // Save
        mongoTemplate.save(newUser);
    }
}
