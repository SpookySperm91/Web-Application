package john.LOGIN_SYSTEM.persistenceMongodb.api;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "#{@mongoDocumentConfig.apiKeys}")
public class ApiKey {
    @Id
    private ObjectId id;
    private String key;
    private Date createdAt;
    private Date expiresAt; // optional data
    private boolean validity;
}
