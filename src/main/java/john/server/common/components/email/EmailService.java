package john.server.common.components.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
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

    @Autowired
    public EmailService(JavaMailSender emailSender, ProcessEmailGenerator processEmail) {
        this.emailSender = emailSender;
        this.processEmail = processEmail;

        templateMapping = new HashMap<>();
        templateMapping.put(TransactionType.REGISTER, "VerificationLink.html");
        templateMapping.put(TransactionType.RESET_PASSWORD, "VerificationCode.html");
    }


    public boolean sendEmail(String username, String userEmail, String body, TransactionType type) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.toString());

            helper.setFrom(new InternetAddress(emailFrom));
            helper.setTo(userEmail);

            String htmlContent = processEmail.processEmailMessage(username, body, getTemplateName(type));
            helper.setText(htmlContent, true);

            emailSender.send(message);

            // If the send method doesn't throw an exception, consider it successful
            return true;
        } catch (MessagingException e) {
            // Log the exception or handle it as needed
            e.printStackTrace();
            return false;
        }
    }

    private String getTemplateName(TransactionType type) {
        return templateMapping.getOrDefault(type, "defaultTemplateName");
    }
}
