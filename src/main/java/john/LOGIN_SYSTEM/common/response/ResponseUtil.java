package john.LOGIN_SYSTEM.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtil {
    public  ResponseEntity<ResponseClient> buildResponse(HttpStatus status, ResponseType type, String message) {
        ResponseClient response = new ResponseClient(type, message);
        return new ResponseEntity<>(response, status);
    }

    public  ResponseEntity<ResponseClient> buildErrorResponse(HttpStatus status, ResponseType type, String message) {
        return buildResponse(status, type, message);
    }
}
