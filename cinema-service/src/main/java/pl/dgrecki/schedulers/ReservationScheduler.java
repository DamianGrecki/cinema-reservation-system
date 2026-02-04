package pl.dgrecki.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.dgrecki.services.ReservationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private static final int UPDATE_LIMIT = 100;

    private final ReservationService reservationService;

    @Scheduled(fixedDelay = 60000)
    public void expireOverdueReservations() {
        int allUpdatedRows = 0;
        int updated;
        do {
            updated = reservationService.setExpireStatusOnOverdueReservations(UPDATE_LIMIT);
            allUpdatedRows = allUpdatedRows + updated;
        } while (updated != 0);
        if (allUpdatedRows > 0) {
            log.info("Expired {} reservations", allUpdatedRows);
        }
    }
}
