package pl.dgrecki.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.dgrecki.handlers.InboxEventHandler;
import pl.dgrecki.models.enums.EventType;

@Configuration
@RequiredArgsConstructor
public class InboxHandlerConfig {

    private final List<InboxEventHandler> handlers;

    @Bean
    public Map<EventType, InboxEventHandler> inboxHandlers() {
        Map<EventType, InboxEventHandler> map = new HashMap<>();
        for (InboxEventHandler handler : handlers) {
            map.put(handler.getSupportedEventType(), handler);
        }
        return map;
    }
}
