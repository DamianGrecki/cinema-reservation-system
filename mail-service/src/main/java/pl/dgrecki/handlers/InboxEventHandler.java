package pl.dgrecki.handlers;

import pl.dgrecki.models.entities.InboxEvent;
import pl.dgrecki.models.enums.EventType;

public interface InboxEventHandler {
    void handle(InboxEvent event);

    EventType getSupportedEventType();
}
