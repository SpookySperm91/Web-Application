package john.LOGIN_SYSTEM.persistenceMongodb.api;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();


    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public void autoGenerateAndSaveKeys(int amount) {
        int i;
        for (i = 0; i < amount; i++) {
            ApiKey key = generateApiKey();
            saveApiKey(key);
            System.out.println("API " + i + ": " + key.getKey());
        }
    }


    public ApiKey generateApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        ApiKey key = new ApiKey();
        key.setKey(base64Encoder.encodeToString(randomBytes));
        key.setCreatedAt(new Date());
        key.setValidity(true);
        return key;
    }


    public void saveApiKey(ApiKey key) {
        apiKeyRepository.save(key);
    }


    public Optional<ApiKey> getApiKey(String key) {
        return apiKeyRepository.findApiKeyByKey(key);
    }


    public Optional<ApiKey> deleteApiKey(String key) {
        return apiKeyRepository.deleteApiKeyByKey(key);
    }


    public Optional<ApiKey> setApiKeyValidity(ApiKey key, boolean validity) {
        if (key == null) {
            return Optional.empty();
        }
        key.setValidity(validity);
        return Optional.of(apiKeyRepository.save(key));
    }
}
