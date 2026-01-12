package pl.dgrecki.schedulers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.handlers.InboxEventDispatcher;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.services.InboxService;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboxProcessor {

    private final InboxEventDispatcher dispatcher;
    private final InboxService inboxService;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void processInbox() {

        List<InboxEvent> events = inboxService.getReceivedInboxEvent();

        for (InboxEvent event : events) {
            try {
                dispatcher.dispatch(event);
                inboxService.markProcessed(event);
            } catch (Exception e) {
                log.error("Failed to process inbox event {}", event.getId(), e);
                inboxService.markFailed(event);
            }
        }
    }
}
