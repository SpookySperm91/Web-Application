package john.LOGIN_SYSTEM.persistenceMongodb.token.verificationCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "#{@mongoDocumentConfig.verificationCode}")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CodeToken implements Serializable {
    @Id
    @JsonIgnore
    private ObjectId id;
    @JsonIgnore
    private String email;
    private String verificationCode;
    @Indexed(name = "tll_index", expireAfterSeconds = 1500)
    private Date createAt;
    private Date expireAt;

    public CodeToken(ObjectId id) {
        this.id = id;
        this.createAt = new Date();
    }
}
