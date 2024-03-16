package john.LOGIN_SYSTEM.session;

import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;



@Service
public class SessionService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    // Method to store a session attribute
    public void setSession(HttpSession session, String attributeName, Object attributeValue) { session.setAttribute(attributeName, attributeValue); }

    // Method to set a session attribute with a custom expiration time
    public void setSession(HttpSession session, String attributeName, Object attributeValue, long expirationInMinutes) {
        session.setAttribute(attributeName, attributeValue);
        String sessionId = session.getId();
        String redisKey = "sessions:" + sessionId + ":" + attributeName;
        redisTemplate.opsForValue().set(redisKey, attributeValue, expirationInMinutes, TimeUnit.MINUTES);
    }

    // Method to retrieve a session attribute
    public Object getSession(HttpSession session, String attributeName) {
        return session.getAttribute(attributeName);
    }

    // Method to remove a session attribute
    public void removeSession(HttpSession session, String attributeName) {
        session.removeAttribute(attributeName);
    }

    // Method to invalidate a session
    public void invalidate(HttpSession session) {
        session.invalidate();
    }
}

