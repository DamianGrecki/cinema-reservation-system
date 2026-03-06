package pl.dgrecki.models.events;

import java.util.UUID;

public record TicketGeneratedEventData(UUID orderId, String userEmail, String userFirstName, String downloadUrl) {}
