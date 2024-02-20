package john.server.register.token;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "verification-link")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LinkToken {
    @Id
    private ObjectId id;
    private String token;
    private LocalDateTime createAt;
    private LocalDateTime expireAt;
    private LocalDateTime confirmedAt;

    public LinkToken(ObjectId id) {
        this.id = id;
    }
}
