package pl.dgrecki.schedulers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.dgrecki.handlers.InboxEventDispatcher;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.services.InboxService;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboxProcessor {

    private static final int EVENTS_LIMIT = 50;

    private final InboxEventDispatcher dispatcher;
    private final InboxService inboxService;

    @Scheduled(fixedDelay = 10000)
    public void processInboxEvents() {
        List<InboxEvent> events = inboxService.claimInboxEvents(EVENTS_LIMIT);
        for (InboxEvent event : events) {
            try {
                dispatcher.dispatch(event);
                inboxService.markProcessed(event);
            } catch (Exception e) {
                log.error("Failed to process inbox event {}", event.getId(), e);
                inboxService.handleFailedAttempt(event, e.getMessage());
            }
        }
    }
}
