package john.LOGIN_SYSTEM.persistenceMongodb.token.verificationCode;

import john.LOGIN_SYSTEM.common.response.ResponseTerminal;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

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

    // RETRIEVE
    public CodeToken getVerificationCode(String code) {
        return tokenRepository.findByVerificationCode(code)
                .orElseThrow(() -> new NoSuchElementException("Verification code not found: " + code));
    }



    // HANDLES EXPIRATION
    public CodeToken handleExpiration(String code) {
        LocalDateTime now = LocalDateTime.now();

        CodeToken token = getVerificationCode(code);
        boolean isExpired = now.isAfter(token.getExpireAt());
        if (isExpired) {
            deleteVerificationCode(token);
            log.status(ResponseType.VERIFICATION_CODE_EXPIRED);
            return null; // Returning null to indicate expiration
        }
        return token; // Returning the original token if not expired
    }
}
