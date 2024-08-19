package john.LOGIN_SYSTEM.persistenceMongodb.token.verificationCode;

import john.LOGIN_SYSTEM.common.response.ResponseTerminal;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

@Service

public class CodeTokenService {
    private final CodeTokenRepository tokenRepository;
    private final ResponseTerminal log;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


    @Autowired
    public CodeTokenService(CodeTokenRepository tokenRepository, ResponseTerminal log) {
        this.tokenRepository = tokenRepository;
        this.log = log;
    }

    // GENERATE VERIFICATION CODE
    public void generateVerificationCode(CodeToken verificationToken) {
        if (verificationToken == null) {
            throw new IllegalArgumentException("verificationToken cannot be null");
        }
        verificationToken.setVerificationCode(generate());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 25);
        verificationToken.setCreateAt(new Date());
        verificationToken.setExpireAt(calendar.getTime());
    }

    private String generate() {
        int codeLength = 6;
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < codeLength; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            code.append(randomChar);
        }
        return code.toString();
    }


    // SAVE
    public void saveVerificationCode(CodeToken token) {
        log.status(ResponseType.VERIFICATION_CODE_GENERATED);
        tokenRepository.save(token);
    }


    // DELETE
    public void deleteVerificationCode(CodeToken token) {
        log.status(ResponseType.VERIFICATION_CODE_DELETED);
        tokenRepository.delete(token);
    }

    // RETRIEVE
    public CodeToken getVerificationCode(String code) {
        return tokenRepository.findByVerificationCode(code)
                .orElseThrow(() -> new NoSuchElementException("Verification code not found: " + code));
    }

    public CodeToken getByEmail(String email) {
        return tokenRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Email instance cannot be found: " + email));
    }


    // HANDLES EXPIRATION
    public CodeToken handleExpiration(String code) {
        CodeToken token = getVerificationCode(code);

        if (new Date().after(token.getExpireAt())) {
            deleteVerificationCode(token);
            log.status(ResponseType.VERIFICATION_CODE_EXPIRED);
            return null; // or an error object if using a different return type
        }
        return token;
    }
}
