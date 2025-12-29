package org.example.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.models.EmailEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Component
public class UserRegistrationMailEventListener {

    private final ObjectMapper objectMapper;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String emailFrom;

    public UserRegistrationMailEventListener(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            ObjectMapper objectMapper,
            @Value("${spring.mail.sender}") String emailFrom) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
        this.emailFrom = emailFrom;
    }

    @SneakyThrows
    @KafkaListener(topics = "${kafka.topics.mail-registration}")
    public void handleUserRegistrationMailEvent(String message) {
        EmailEvent event = objectMapper.readValue(message, EmailEvent.class);
        sendEmail(event);
    }

    @SneakyThrows
    private void sendEmail(EmailEvent event) {
        Context context = new Context();
        context.setVariables(event.getData());

        String htmlContent = templateEngine.process(event.getTemplate(), context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
        messageHelper.setFrom(emailFrom);
        messageHelper.setTo(event.getTo());
        messageHelper.setSubject(event.getSubject());
        messageHelper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Sent email to '{}', EventType: '{}'", event.getTo(), event.getEventType());
    }
}
