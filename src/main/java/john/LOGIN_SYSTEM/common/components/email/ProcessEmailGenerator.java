package john.LOGIN_SYSTEM.common.components.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
class ProcessEmailGenerator {
    private final TemplateEngine templateEngine;

    @Autowired
    public ProcessEmailGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String processEmailMessage(String username, String body, String transactionType) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("body", body);

        // Process the Thymeleaf template
        return templateEngine.process(transactionType, context);
    }
}
