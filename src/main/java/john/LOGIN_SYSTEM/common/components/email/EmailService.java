package john.LOGIN_SYSTEM.common.components.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import john.LOGIN_SYSTEM.common.response.ResponseTerminal;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailService {
    private final JavaMailSender emailSender;
    private final ProcessEmailGenerator processEmail;
    @Value("${spring.mail.username}")
    private String emailFrom;
    private final Map<TransactionType, String> templateMapping;
    private final ResponseTerminal log;

    @Autowired
    public EmailService(JavaMailSender emailSender, ProcessEmailGenerator processEmail, ResponseTerminal log) {
        this.emailSender = emailSender;
        this.processEmail = processEmail;
        this.log = log;

        templateMapping = new HashMap<>();
        templateMapping.put(TransactionType.REGISTER, "VerificationLink.html");
        templateMapping.put(TransactionType.RESET_PASSWORD, "VerificationCode.html");
    }


    public void sendEmail(String username, String userEmail, String body, TransactionType type) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.toString());

            helper.setFrom(new InternetAddress(emailFrom));
            helper.setTo(userEmail);

            String htmlContent = processEmail.processEmailMessage(username, body, getTemplateName(type));
            helper.setText(htmlContent, true);

            emailSender.send(message);
            log.success(switch (type) {
                case REGISTER -> ResponseType.EMAIL_VERIFICATION_LINK_SUCCESS;
                case RESET_PASSWORD -> ResponseType.EMAIL_VERIFICATION_CODE_SUCCESS;
            });

            // If the send method doesn't throw an exception, consider it successful
        } catch (MessagingException e) {
            // Log the exception or handle it as needed
            System.out.println("ERROR: Email service is not working");
        }
    }

    // Retrieve a specific html email message
    private String getTemplateName(TransactionType type) {
        return templateMapping.getOrDefault(type, "defaultTemplateName");
    }
}
