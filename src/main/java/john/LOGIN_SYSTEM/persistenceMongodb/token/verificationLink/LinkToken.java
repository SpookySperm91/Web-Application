package john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "verification-link")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LinkToken implements Serializable {
    @Id
    @JsonIgnore
    private ObjectId id;
    @JsonIgnore
    private String token;
    private LocalDateTime createAt;
    private LocalDateTime expireAt;
    private LocalDateTime confirmedAt;

    public LinkToken(ObjectId id) {
        this.id = id;
    }
}
