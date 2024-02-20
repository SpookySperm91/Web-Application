package john.server.register.token;

import john.server.common.response.ResponseTerminal;
import john.server.common.response.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LinkTokenService {
    private final LinkTokenRepository tokenRepository;
    private final ResponseTerminal log;

    @Autowired
    public LinkTokenService(LinkTokenRepository tokenRepository, ResponseTerminal terminal) {
        this.tokenRepository = tokenRepository;
        this.log = terminal;
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
        LocalDateTime now = LocalDateTime.now();

        boolean isExpired = now.isAfter(token.getExpireAt());
        if (isExpired) {
            deleteVerificationLink(token);
            log.status(ResponseType.LINK_EXPIRED);
            return null; // Returning null to indicate expiration
        }
        return token; // Returning the original token if not expired
    }
}
