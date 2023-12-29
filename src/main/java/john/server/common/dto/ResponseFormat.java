package john.server.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

// RESPONSE BACK TO THE CLIENT/FRONTEND AS A JSON FORMAT
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseFormat {
    private ResponseType responseType;
    private String responseText;
    private HttpStatus httpStatus;
}



