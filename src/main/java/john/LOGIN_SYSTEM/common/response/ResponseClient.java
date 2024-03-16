package john.LOGIN_SYSTEM.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// RESPONSE BACK TO THE CLIENT/FRONTEND AS A JSON FORMAT
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseClient {
    private ResponseType responseType;
    private String responseText;
}






