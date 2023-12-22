package john.server.common.dto;

import john.server.repository_entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckUserInput {
    private boolean success;
    private String message;
    private UserEntity userEntity;

    public CheckUserInput(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public CheckUserInput(Boolean success, UserEntity userEntity){
        this.success = success;
        this.userEntity = userEntity;
    }
    public CheckUserInput(Boolean success){
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

}
