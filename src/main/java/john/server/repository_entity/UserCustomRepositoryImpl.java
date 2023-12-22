package john.server.repository_entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserCustomRepositoryImpl
        implements UserCustomRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserCustomRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        // Define the query criteria to find a document with the specified email
        Query query = new Query(Criteria.where("email").is(email));

        // Use the MongoTemplate to execute the query and get the result
        UserEntity result = mongoTemplate.findOne(query, UserEntity.class);

        // Convert the result to an Optional
        return Optional.ofNullable(result);
    }


    @Override
    public UserEntity saveUserAccount(String username,
                                String hashedPassword, String salt,
                                String email,
                                LocalDateTime accountDateCreated) {
        // Create instance
        UserEntity newUser = new UserEntity(null, username, hashedPassword, salt, email, accountDateCreated);

        // Save
        return mongoTemplate.save(newUser);
    }


    @Override
    public Optional<UserEntity> updatePassword(UserEntity user, String newPassword) {
        user.setPassword(newPassword);
        return Optional.of(mongoTemplate.save(user));
    }
}
