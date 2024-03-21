package john.LOGIN_SYSTEM.common.components;

import john.LOGIN_SYSTEM.common.response.ResponseTerminal;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.register.token.LinkToken;
import john.LOGIN_SYSTEM.repository.entity.user.UserRepository;
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
    public void enableAccount(LinkToken user) {
        repository.findById(user.getId())
                .ifPresent(userEntity -> {
                    repository.enableAccount(userEntity);
                    log.status(ResponseType.ACCOUNT_VALID);
                });
    }
}
