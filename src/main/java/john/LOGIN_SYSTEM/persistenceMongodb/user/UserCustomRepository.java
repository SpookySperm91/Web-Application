package john.LOGIN_SYSTEM.persistenceMongodb.user;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCustomRepository {
    UserEntity generateUserID();


    void saveUserAccount(UserEntity pendingUser);


    Optional<UserEntity> updatePassword(UserEntity user, String password);


    void enableAccount(UserEntity user);

}

