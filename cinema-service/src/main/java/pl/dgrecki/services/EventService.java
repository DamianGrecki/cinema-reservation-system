package pl.dgrecki.services;

import static pl.dgrecki.models.enums.EventType.TICKET_GENERATED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.events.TicketGeneratedEventData;

@Service
@RequiredArgsConstructor
public class EventService {

    private final OutboxService outboxService;
    private final TicketDownloadUrlService ticketDownloadUrlService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createTicketGeneratedEvent(UUID orderId, String recipientEmail) {
        String downloadUrl = ticketDownloadUrlService.generateUrl(orderId);
        TicketGeneratedEventData data = new TicketGeneratedEventData(orderId, recipientEmail, downloadUrl);
        outboxService.createOutboxEvent(TICKET_GENERATED, toJson(data));
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event data", e);
        }
    }
}
