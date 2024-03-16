package john.LOGIN_SYSTEM.repository.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import john.LOGIN_SYSTEM.register.token.LinkToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user-account-data")
@AllArgsConstructor
@Data
public class UserEntity {
    @Id
    @JsonIgnore
    private ObjectId id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String salt;
    @JsonIgnore
    private LocalDateTime accountDateCreated;
    @JsonIgnore
    private boolean enabled;
    @JsonIgnore
    private boolean logged;
    @JsonIgnore
    private LinkToken token;


    public UserEntity() {
        this.enabled = false;
        this.logged = false;
    }
}


