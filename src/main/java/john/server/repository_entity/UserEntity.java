package john.server.repository_entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user-account-data")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserEntity {
    @JsonIgnore
    @Id
    private ObjectId id;
    private String username;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String salt;
    private String email;
    private LocalDateTime accountDateCreated;
}
