package john.LOGIN_SYSTEM.common.response;

import john.LOGIN_SYSTEM.repository.entity.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

// RESPONSE BACK TO THE CONTROLLER FROM THE SERVICE
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseLayer {
    @Getter
    private boolean success;
    private String message;
    private UserEntity userEntity;
    private HttpStatus httpStatus;

    public ResponseLayer(Boolean success, String message, HttpStatus httpStatus) {
        this.success = success;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public ResponseLayer(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ResponseLayer(Boolean success, UserEntity userEntity) {
        this.success = success;
        this.userEntity = userEntity;
    }

    public ResponseLayer(Boolean success) {
        this.success = success;
    }
}
