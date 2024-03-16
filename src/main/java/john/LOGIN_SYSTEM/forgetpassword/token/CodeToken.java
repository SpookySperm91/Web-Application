package john.LOGIN_SYSTEM.forgetpassword.token;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private ObjectId id;
    @JsonIgnore
    private String verificationCode;
    private LocalDateTime createAt;
    private LocalDateTime expireAt;

    public CodeToken(ObjectId id) { this.id = id; }
}
