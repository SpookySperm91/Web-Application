package john.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

// RESPONSE BACK TO THE CLIENT/FRONTEND AS A JSON FORMAT
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseClient {
    private ResponseType responseType;
    private String responseText;
}






