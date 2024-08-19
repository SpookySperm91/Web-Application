package john.LOGIN_SYSTEM.common.exception;

public class RedisConnectionException extends RuntimeException {
    public RedisConnectionException(String message) {
        super(message);
    }
}
