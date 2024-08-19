package john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import john.LOGIN_SYSTEM.common.config.RedisConfig;
import john.LOGIN_SYSTEM.persistenceMongodb.token.VerificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "#{@mongoDocumentConfig.verificationLink}")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LinkToken implements Serializable {
    @Id
    @JsonSerialize(using = RedisConfig.ObjectIdSerializer.class)
    @JsonDeserialize(using = RedisConfig.ObjectIdDeserializer.class)
    private ObjectId id;
    private String token;
    @JsonIgnore
    private VerificationType type;
    private Date createAt;
    @Indexed(name = "tll_index", expireAfterSeconds = 600)
    private Date expireAt;
    private Date confirmedAt;

    public LinkToken(ObjectId id) {
        this.id = id;
    }

    public LinkToken(ObjectId id, VerificationType type) {
        this.id = id;
        this.type = type;
    }
}
