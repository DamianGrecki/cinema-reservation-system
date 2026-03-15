package pl.dgrecki.listeners;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import pl.dgrecki.services.InboxDlqService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDlqListener {

    private final InboxDlqService inboxDlqService;

    @KafkaListener(topics = {"${kafka.topics.user-registration-dlq}", "${kafka.topics.ticket-generated-dlq}"})
    public void handleDlqEvent(
            String rawMessage, Acknowledgment acknowledgment, ConsumerRecord<String, String> record) {
        log.warn(
                "Received DLQ event on topic {}, partition {}, key {}",
                record.topic(),
                record.partition(),
                record.key());
        String errorMessage = getErrorMessage(record);
        inboxDlqService.createInboxDlqEvent(record.topic(), record.partition(), rawMessage, errorMessage);
        acknowledgment.acknowledge();
    }

    private String getErrorMessage(ConsumerRecord<String, String> record) {
        if (record.headers() != null) {
            Header header = record.headers().lastHeader("x-error-message");
            if (header != null) {
                return new String(header.value(), StandardCharsets.UTF_8);
            }
        }
        return "";
    }
}
