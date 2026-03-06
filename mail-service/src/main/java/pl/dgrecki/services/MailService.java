package pl.dgrecki.services;

import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import pl.dgrecki.models.Attachment;
import pl.dgrecki.models.MailData;

@Slf4j
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String emailFrom;
    private final SpringTemplateEngine templateEngine;

    public MailService(JavaMailSender mailSender, @Value("${spring.mail.sender}") String emailFrom) {
        this.mailSender = mailSender;
        this.emailFrom = emailFrom;
        this.templateEngine = getTemplateEngine();
    }

    @SneakyThrows
    public void send(MailData mailData) {
        try {
            Context context = new Context();
            context.setVariables(mailData.getVariables());

            String htmlContent = templateEngine.process(mailData.getTemplateHtml(), context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setFrom(emailFrom);
            messageHelper.setTo(mailData.getTo());
            messageHelper.setSubject(mailData.getSubject());
            messageHelper.setText(htmlContent, true);
            addAttachments(messageHelper, mailData.getAttachments());

            mailSender.send(message);
            log.info("Sent email to '{}', Template: '{}'", mailData.getTo(), mailData.getTemplateName());
        } catch (Exception ex) {
            log.error("Sending email failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @SneakyThrows
    private void addAttachments(MimeMessageHelper messageHelper, List<Attachment> attachments) {
        if (Objects.nonNull(attachments)) {
            for (Attachment attachment : attachments) {
                messageHelper.addAttachment(
                        attachment.fileName(), new ByteArrayResource(attachment.content()), attachment.contentType());
            }
        }
    }

    private SpringTemplateEngine getTemplateEngine() {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("HTML");
        resolver.setCacheable(false);

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }
}
