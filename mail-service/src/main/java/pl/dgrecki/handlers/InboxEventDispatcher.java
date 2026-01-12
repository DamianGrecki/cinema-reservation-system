package pl.dgrecki.handlers;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.models.enums.EventType;

@Component
@RequiredArgsConstructor
public class InboxEventDispatcher {

    private final Map<EventType, InboxEventHandler> handlers;

    public void dispatch(InboxEvent event) {
        InboxEventHandler handler = handlers.get(event.getEventType());

        if (handler == null) {
            throw new IllegalStateException("No handler for event type " + event.getEventType());
        }

        handler.handle(event);
    }
}
