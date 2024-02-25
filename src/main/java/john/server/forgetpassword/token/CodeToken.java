package john.server.forgetpassword.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "verification-code")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CodeToken implements Serializable {
    @Id
    private ObjectId id;
    private String verificationCode;
    private LocalDateTime createAt;
    private LocalDateTime expireAt;
    private LocalDateTime confirmedAt;

    public CodeToken(ObjectId id) { this.id = id; }
}
