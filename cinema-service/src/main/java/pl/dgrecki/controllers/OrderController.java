package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.ORDERS_HISTORY_ENDPOINT;
import static pl.dgrecki.constants.Endpoints.ORDER_TICKETS_DOWNLOAD_ENDPOINT;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.config.AuthenticatedUser;
import pl.dgrecki.models.responses.OrderHistoryResponse;
import pl.dgrecki.services.OrderHistoryService;
import pl.dgrecki.services.TicketDownloadService;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final TicketDownloadService ticketDownloadService;
    private final OrderHistoryService orderHistoryService;

    @GetMapping(ORDER_TICKETS_DOWNLOAD_ENDPOINT)
    public ResponseEntity<byte[]> downloadOrderTickets(@PathVariable UUID orderId, @RequestParam String signature) {

        byte[] zipContent = ticketDownloadService.getTicketsZip(orderId, signature);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tickets_" + orderId + ".zip\"")
                .contentType(MediaType.valueOf("application/zip"))
                .contentLength(zipContent.length)
                .body(zipContent);
    }

    @GetMapping(ORDERS_HISTORY_ENDPOINT)
    public ResponseEntity<List<OrderHistoryResponse>> getMyHistory(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(orderHistoryService.getHistoryForUser(user.getUserId()));
    }
}
