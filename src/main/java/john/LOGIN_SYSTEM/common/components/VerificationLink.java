package john.LOGIN_SYSTEM.common.components;

import john.LOGIN_SYSTEM.register.token.LinkTokenService;
import john.LOGIN_SYSTEM.register.token.LinkToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
// @CacheConfig(cacheNames = {"verification-link"})
public class VerificationLink {
    private final LinkTokenService tokenService;

    @Autowired
    public VerificationLink(LinkTokenService tokenService) {
        this.tokenService = tokenService;
    }


    // @Cacheable(key = "#pendingToken.token")
    public void generateToken(LinkToken pendingToken) {
        if (pendingToken == null) {
            throw new IllegalArgumentException("pendingToken cannot be null");
        }

        String verificationToken = UUID.randomUUID().toString();

        pendingToken.setToken(verificationToken);
        pendingToken.setCreateAt(LocalDateTime.now());
        pendingToken.setExpireAt(LocalDateTime.now().plusMinutes(25));

        tokenService.saveVerificationLink(pendingToken);
    }


    // GENERATE LINK
    public String generateLink(String token) {
        String baseUrl = "http://localhost:8080/";
        String controllerPoint = "api/register/";
        return baseUrl + controllerPoint + token;
    }
}
