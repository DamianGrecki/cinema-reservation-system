package pl.dgrecki.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketGeneratedEventData {
    private String userEmail;
    private String userFirstName;
    private UUID orderId;
    private String downloadUrl;
}
