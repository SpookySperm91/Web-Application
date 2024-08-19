package john.LOGIN_SYSTEM.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class PropertiesConfig {
    @Value("${server.port.base-url}")
    private String baseUrl;
}
