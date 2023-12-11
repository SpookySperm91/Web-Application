package john.server.repository_entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserEntity {
    @Id
    private ObjectId id;
    private String username;
    private String hashedPassword;
    private String salt;
    private String email;
    private LocalDateTime accountDateCreated;
}
