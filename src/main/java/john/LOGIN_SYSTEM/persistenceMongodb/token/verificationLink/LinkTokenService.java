package john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink;

import john.LOGIN_SYSTEM.common.response.ResponseTerminal;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.persistenceMongodb.token.VerificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@PropertySource("classpath:application.properties")
public class LinkTokenService {
    @Value("${server.front-end.base-url}")
    private String gatewayURL;
    @Value("${link.change-password.path}")
    private String changePasswordPath;
    @Value("${link.create-account.path}")
    private String createAccountPath;
    private final LinkTokenRepository tokenRepository;
    private final ResponseTerminal log;


    @Autowired
    public LinkTokenService(LinkTokenRepository tokenRepository, ResponseTerminal terminal) {
        this.tokenRepository = tokenRepository;
        this.log = terminal;
    }

    // INSTANTIATE TOKEN DATA
    public void generateToken(LinkToken token) {
        if (token == null) {
            throw new IllegalArgumentException("pendingToken cannot be null");
        }
        String verificationToken = UUID.randomUUID().toString();
        token.setToken(verificationToken);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 25);

        token.setCreateAt(new Date());
        token.setExpireAt(calendar.getTime());
    }


    // GENERATE LINK
    public String generateLink(String token, VerificationType type) {
        return switch (type) {
            case CREATE_USER -> gatewayURL + createAccountPath + token;
            case CHANGE_PASSWORD_VIA_EMAIL -> gatewayURL + changePasswordPath + token;
        };
    }


    // SAVE TOKEN
    public void saveVerificationLink(LinkToken token) {
        log.status(ResponseType.LINK_GENERATED);
        tokenRepository.save(token);
    }


    // DELETE TOKEN
    public void deleteVerificationLink(LinkToken token) {
        log.status(ResponseType.LINK_DELETED);
        tokenRepository.delete(token);
    }


    // RETRIEVE TOKEN
    public Optional<LinkToken> getVerificationLink(String linkToken) {
        return tokenRepository.findByToken(linkToken).map(this::handleExpiration);
    }

    private LinkToken handleExpiration(LinkToken token) {
        boolean isExpired = new Date().after(token.getExpireAt());
        if (isExpired) {
            deleteVerificationLink(token);
            log.status(ResponseType.LINK_EXPIRED);
            return null; // Returning null to indicate expiration
        }
        return token; // Returning the original token if not expired
    }
}
