package john.LOGIN_SYSTEM.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import john.LOGIN_SYSTEM.persistenceMongodb.api.ApiKey;
import john.LOGIN_SYSTEM.persistenceMongodb.api.ApiKeyService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {
    private final ApiKeyService apiKeyService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public ApiKeyInterceptor(ApiKeyService apiKeyService, RedisTemplate<String, Object> redisTemplate) {
        this.apiKeyService = apiKeyService;
        this.redisTemplate = redisTemplate;
    }

    // INTERCEPTOR FOR API KEYS
    // Check API keys if valid
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || !isValidApiKey(apiKey)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Unauthorized: Invalid API key!");
            return false;
        }
        if (!apiKeyInSession(apiKey)) {
            redisTemplate.opsForValue().set("api-key:" + apiKey, true);
        }
        return true;
    }

    private Boolean apiKeyInSession(String key) {
        Boolean isValid = (Boolean) redisTemplate.opsForValue().get("api-key:" + key);
        return Boolean.TRUE.equals(isValid);
    }

    private boolean isValidApiKey(String apiKey) {
        return apiKeyService.getApiKey(apiKey)
                .filter(ApiKey::isValidity)
                .isPresent();
    }
}
