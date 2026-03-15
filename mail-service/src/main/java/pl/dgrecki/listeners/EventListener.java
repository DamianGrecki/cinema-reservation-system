package pl.dgrecki.listeners;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.dgrecki.exceptions.HmacVerificationException;
import pl.dgrecki.kafka.HmacSigner;
import pl.dgrecki.models.IncomingEvent;
import pl.dgrecki.services.InboxService;
import pl.dgrecki.services.IncomingEventDeserializer;

@Slf4j
@Component
public class EventListener {

    private final InboxService inboxService;
    private final IncomingEventDeserializer incomingEventDeserializer;
    private final HmacSigner hmacSigner;

    public EventListener(
            InboxService inboxService,
            IncomingEventDeserializer incomingEventDeserializer,
            @Value("${spring.kafka.hmac-secret}") String hmacSecret) {
        this.inboxService = inboxService;
        this.incomingEventDeserializer = incomingEventDeserializer;
        this.hmacSigner = new HmacSigner(hmacSecret);
    }

    @KafkaListener(topics = {"${kafka.topics.user-registration}", "${kafka.topics.ticket-generated}"})
    public void handleIncomingEvent(
            @Payload String message, ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        verifyHmac(message, consumerRecord);
        IncomingEvent incomingEvent = incomingEventDeserializer.deserialize(message);
        inboxService.createInboxEvent(incomingEvent);
        acknowledgment.acknowledge();
    }

    private void verifyHmac(String message, ConsumerRecord<String, String> consumerRecord) {
        Header hmacHeader = consumerRecord.headers().lastHeader(HmacSigner.HMAC_HEADER);
        if (hmacHeader == null) {
            throw new HmacVerificationException("Missing HMAC signature for event on topic %s, key %s"
                    .formatted(consumerRecord.topic(), consumerRecord.key()));
        }
        String signature = new String(hmacHeader.value(), StandardCharsets.UTF_8);
        if (!hmacSigner.verify(message, signature)) {
            throw new HmacVerificationException("Invalid HMAC signature for event on topic %s, key %s"
                    .formatted(consumerRecord.topic(), consumerRecord.key()));
        }
    }
}
