package john.LOGIN_SYSTEM.repository.entity.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserCustomRepositoryImpl
        implements UserCustomRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserCustomRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    // CREATE USER ID FOR CACHE -> VERIFICATION PROCESS.
    @Override
    public UserEntity generateUserID() {
        // Create id
        return new UserEntity();
    }

    @Override
    public void saveUserAccount(UserEntity pendingUser){
        mongoTemplate.save(pendingUser);
    }


    @Override
    public Optional<UserEntity> updatePassword(UserEntity user, String newPassword) {
        user.setPassword(newPassword);
        return Optional.of(mongoTemplate.save(user));
    }


    @Override
    public void enableAccount(UserEntity user) {
        user.setEnabled(true);
        mongoTemplate.save(user);
    }
}

