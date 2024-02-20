package john.server.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "session-manager")
@AllArgsConstructor
@Data
public class SessionEntity {
    @Id
    private ObjectId id;
    private SessionType type;
    private ObjectId userId;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private LocalDateTime lastAccessAt;
}
