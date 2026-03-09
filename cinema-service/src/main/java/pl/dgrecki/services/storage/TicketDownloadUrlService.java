package pl.dgrecki.services.storage;

import java.util.UUID;

public interface TicketDownloadUrlService {

    String generateUrl(UUID orderId);

    boolean isValidSignature(UUID orderId, String signature);
}
