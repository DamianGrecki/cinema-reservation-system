package pl.dgrecki.services;

import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.dgrecki.models.MailData;

@Slf4j
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String emailFrom;

    public MailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            @Value("${spring.mail.sender}") String emailFrom) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailFrom = emailFrom;
    }

    @SneakyThrows
    public void send(MailData mailData) {
        try {
            Context context = new Context();
            context.setVariables(mailData.getVariables());

            String htmlContent = templateEngine.process(mailData.getTemplate(), context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setFrom(emailFrom);
            messageHelper.setTo(mailData.getTo());
            messageHelper.setSubject(mailData.getSubject());
            messageHelper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent email to '{}', Template: '{}'", mailData.getTo(), mailData.getTemplate());
        } catch (Exception ex) {
            log.error("Sending email failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
