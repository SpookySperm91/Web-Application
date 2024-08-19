package john.LOGIN_SYSTEM.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("classpath:application.properties")
public class MongoDocumentConfig {

    @Value("${mongodb.collection.verification-link}")
    private String verificationLink;

    @Value("${mongodb.collection.verification-code}")
    private String verificationCode;

    @Value("${mongodb.collection.user-account-data}")
    private String userAccountData;

    @Value("${mongodb.collection.api-keys}")
    private String apiKeys;
}