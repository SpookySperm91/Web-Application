package john.server.repository_entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "userData")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDataEntity {
    private String username;
    private String hashedPassword;
    private String salt;
    private String emailAccount;
    private LocalDateTime accountDateCreated;
}
