package john.server.common.components;

import john.server.forgetpassword.token.CodeToken;
import john.server.forgetpassword.token.CodeTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class VerificationCode {
    private final CodeTokenService tokenService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Autowired
    public VerificationCode(CodeTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public void generateVerificationCode(CodeToken verificationToken) {
        if (verificationToken == null) {
            throw new IllegalArgumentException("verificationToken cannot be null");
        }

        verificationToken.setVerificationCode(generate());
        verificationToken.setCreateAt(LocalDateTime.now());
        verificationToken.setExpireAt(LocalDateTime.now().plusMinutes(5));

        tokenService.saveVerificationCode(verificationToken);
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
}
