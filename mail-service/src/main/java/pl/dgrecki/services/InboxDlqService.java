package pl.dgrecki.services;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.InboxEventDlq;
import pl.dgrecki.repositories.InboxEventDlqRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboxDlqService {

    private final InboxEventDlqRepository inboxDlqRepository;

    @Transactional
    public void createInboxDlqEvent(String topic, int partition, String rawMessage, String errorMessage) {
        log.warn("Saving DLQ event from topic {} partition {}: {}", topic, partition, errorMessage);
        InboxEventDlq event = InboxEventDlq.builder()
                .topic(topic)
                .partition(partition)
                .rawMessage(rawMessage)
                .errorMessage(errorMessage)
                .createdAt(Instant.now())
                .build();
        inboxDlqRepository.save(event);
    }
}
