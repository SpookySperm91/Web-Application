package john;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;


@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class WebApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(WebApplication.class, args);
    }
}
