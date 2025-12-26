package org.example.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.models.EmailPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegistrationMailEventListener {

    private final ObjectMapper objectMapper;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.sender}")
    private String from;

    @SneakyThrows
    @KafkaListener(topics = "${kafka.topics.mail-registration}")
    public void handleUserRegistrationMailEvent(String message) {
        EmailPayload payload = objectMapper.readValue(message, EmailPayload.class);
        sendEmail(payload);
    }

    private void sendEmail(EmailPayload payload) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(payload.getTo());
        message.setSubject(payload.getSubject());
        message.setText(payload.getBody());
        mailSender.send(message);
        log.info("Sent email to '{}' with subject '{}'", payload.getTo(), payload.getSubject());
    }
}
