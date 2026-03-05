package pl.dgrecki.services.storage;

import java.util.UUID;

public interface TicketFileStorage {

    String store(UUID ticketId, byte[] pdfContent);

    byte[] load(String fileName);
}
