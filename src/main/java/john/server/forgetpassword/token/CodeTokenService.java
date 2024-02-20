package john.server.forgetpassword.token;

import john.server.common.response.ResponseTerminal;
import john.server.common.response.ResponseType;
import org.bson.types.Code;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service

public class CodeTokenService {
    private final CodeTokenRepository tokenRepository;
    private final ResponseTerminal log;

    @Autowired
    public CodeTokenService(CodeTokenRepository tokenRepository, ResponseTerminal log) {
        this.tokenRepository = tokenRepository;
        this.log = log;
    }


    // SAVE
    public void saveVerificationCode(CodeToken token){
        log.status(ResponseType.VERIFICATION_CODE_GENERATED);
        tokenRepository.save(token);
    }


    // DELETE
    public void deleteVerificationCode(CodeToken token){
        log.status(ResponseType.VERIFICATION_CODE_DELETED);
        tokenRepository.delete(token);
    }

    public String getVerificationCodeById(ObjectId id) {
        return tokenRepository.findById(id).get().getVerificationCode();
    }

    // RETRIEVE TOKEN
    public Optional<CodeToken> getVerificationCode(String token) {
        return tokenRepository.findByVerificationCode(token).map(this::handleExpiration);
    }

    private CodeToken handleExpiration(CodeToken token) {
        LocalDateTime now = LocalDateTime.now();

        boolean isExpired = now.isAfter(token.getExpireAt());
        if (isExpired) {
            deleteVerificationCode(token);
            log.status(ResponseType.VERIFICATION_CODE_EXPIRED);
            return null; // Returning null to indicate expiration
        }
        return token; // Returning the original token if not expired
    }
}
