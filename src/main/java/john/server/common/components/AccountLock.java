package john.server.common.components;

import john.server.common.response.ResponseTerminal;
import john.server.common.response.ResponseType;
import john.server.forgetpassword.token.CodeToken;
import john.server.register.token.LinkToken;
import john.server.repository.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountLock {
    private final UserRepository repository;
    private final ResponseTerminal log;

    @Autowired
    public AccountLock(UserRepository repository, ResponseTerminal terminal) {
        this.repository = repository;
        this.log = terminal;
    }


    // ENABLE ACCOUNT
    public void enableAccount(LinkToken token) {
        repository.findById(token.getId())
                .ifPresent(userEntity -> {
                    repository.enableAccount(userEntity);
                    log.status(ResponseType.ACCOUNT_VALID);
                });
    }
}
