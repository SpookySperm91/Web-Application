package john.LOGIN_SYSTEM.common.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResponseTerminal {
    private final Logger logger = LoggerFactory.getLogger(ResponseTerminal.class);

    // SUCCESS
    public void success(ResponseType type) {
        logger.info(switch (type) {
            case LOGIN_SUCCESS -> ("LOGIN SERVICE STATUS: SUCCESS, Login successful.");
            case SIGNUP_SUCCESS -> ("SIGNUP SERVICE STATUS: SUCCESS, Signup successful.");
            case RESET_PASSWORD_SUCCESS -> ("RESET PASSWORD SERVICE STATUS: SUCCESS, Password reset successful.");
            case VERIFICATION_SUCCESS -> ("VERIFICATION SERVICE STATUS: SUCCESS, Verification successful.");
            case EMAIL_VERIFICATION_CODE_SUCCESS -> ("EMAIL STATUS: SUCCESS, Verification code is sent");
            case EMAIL_VERIFICATION_LINK_SUCCESS -> ("EMAIL STATUS: SUCCESS, Verification link is sent");
            default -> throw new IllegalArgumentException("Unexpected response type: " + type);
        });
    }

    // FAIL
    public void fail(ResponseType type) {
        logger.error(switch (type) {
            case LOGIN_ERROR -> ("LOGIN SERVICE STATUS: ERROR, Login fail.");
            case SIGNUP_ERROR -> ("SIGNUP SERVICE STATUS: ERROR, Signup fail.");
            case RESET_PASSWORD_ERROR -> ("RESET PASSWORD SERVICE STATUS: ERROR, Password reset fail.");
            case VERIFICATION_ERROR -> ("VERIFICATION SERVICE STATUS: ERROR, Verification fail.");
            default -> throw new IllegalArgumentException("Unexpected response type: " + type);
        });
    }

    // STATUS
    public void status(ResponseType type) {
        logger.warn(switch (type) {
            // user status
            case ACCOUNT_VALID -> ("ACCOUNT STATUS: VALID");
            case ACCOUNT_RESTRICTED-> ("ACCOUNT STATUS: RESTRICTED");
            case ACCOUNT_BANNED -> ("ACCOUNT STATUS: BANNED");
            case SIGNUP_PENDING -> ("SIGNUP PENDING");

            // query status
            case LINK_GENERATED -> ("DATABASE QUERY STATUS: TOKEN IS GENERATED");
            case LINK_DELETED -> ("DATABASE QUERY STATUS: TOKEN DELETED");
            case LINK_EXPIRED -> ("DATABASE QUERY STATUS: TOKEN EXPIRED");
            case VERIFICATION_CODE_GENERATED-> ("DATABASE QUERY STATUS: VERIFICATION CODE GENERATED");
            case VERIFICATION_CODE_DELETED -> ("DATABASE QUERY STATUS: VERIFICATION CODE DELETED");
            case VERIFICATION_CODE_EXPIRED -> ("DATABASE QUERY STATUS: VERIFICATION CODE EXPIRED, AUTO DELETE");
            case ACCOUNT_EXIST ->("DATABASE QUERY STATUS: ACCOUNT DATA EXIST");
            case ACCOUNT_NOT_EXIST -> ("DATABASE QUERY STATUS: ACCOUNT DATA DOES NOT EXIST");
            default -> throw new IllegalArgumentException("Unexpected response type: " + type);
        });
    }
}
