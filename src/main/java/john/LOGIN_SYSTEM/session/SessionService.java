package john.LOGIN_SYSTEM.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Service
public class SessionService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    // Method to generate a Redis key for a session
    private String getRedisKey(HttpSession session, String attributeName) {
        return "sessions:" + session.getId() + ":" + attributeName;
    }


    // Method to store a session attribute in Redis
    public void setSession(HttpSession session, String attributeName, Object attributeValue, long expirationInMinutes) {
        String redisKey = getRedisKey(session, attributeName);
        redisTemplate.opsForValue().set(redisKey, attributeValue, expirationInMinutes, TimeUnit.MINUTES);
    }


    // Method to store a session attribute in Redis
    public void setSession(HttpSession session, String attributeName, Object attributeValue) {
        String redisKey = getRedisKey(session, attributeName);
        redisTemplate.opsForValue().set(redisKey, attributeValue);
    }


    // Method to retrieve a session attribute from Redis
    public Object getSession(HttpSession session, String attributeName) {
        String redisKey = getRedisKey(session, attributeName);
        return redisTemplate.opsForValue().get(redisKey);
    }


    // Method to store a session attribute as a hash in Redis
    public void setSessionHash(HttpSession session, String attributeName, Map<String, Object> attributeValue, long expirationInMinutes) {
        String redisKey = getRedisKey(session, attributeName);
        System.out.println("Setting Redis Key: " + redisKey + " for Session ID: " + session.getId());
        redisTemplate.opsForHash().putAll(redisKey, attributeValue);
        redisTemplate.expire(redisKey, expirationInMinutes, TimeUnit.MINUTES);
    }


    // Method to retrieve a session hash from Redis
    public Map<String, Object> getSessionHash(HttpSession session, String attributeName) {
        String redisKey = getRedisKey(session, attributeName);
        System.out.println("Retrieving Redis Key: " + redisKey + " for Session ID: " + session.getId());
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        return hashOps.entries(redisKey);
    }


    // Method to retrieve a specific value from a session hash in Redis
    public Object getSessionHashValue(HttpSession session, String attributeName, String keyToRetrieve) {
        Map<String, Object> sessionHash = getSessionHash(session, attributeName);
        return sessionHash != null ? sessionHash.get(keyToRetrieve) : null;
    }


    // Method to remove a session attribute from Redis
    public void removeSession(HttpSession session, String attributeName) {
        String redisKey = getRedisKey(session, attributeName);
        redisTemplate.delete(redisKey);
    }


    // Method to invalidate a session
    public void invalidate(HttpSession session) {
        // Invalidation here would be context-dependent; assuming session is managed in Redis only
        // This method may clear all session-related data in Redis or perform other actions if necessary
        String redisKeyPattern = "sessions:" + session.getId() + ":*";
        redisTemplate.delete(Objects.requireNonNull(redisTemplate.keys(redisKeyPattern)));
    }


    // Method to convert an object to a Map
    public Map<String, Object> convertToMap(Object object) throws Exception {
        return objectMapper.convertValue(object, Map.class);
    }

}

