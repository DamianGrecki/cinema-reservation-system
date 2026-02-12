package pl.dgrecki.schedulers;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.services.BasketService;
import pl.dgrecki.services.ReservationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;
    private final BasketService basketService;

    @Value("${scheduler.reservation.update.limit}")
    private int updateLimit;

    @Value("${scheduler.reservation.delete.limit}")
    private int deleteLimit;

    @Value("${scheduler.reservation.delete.older-than-days}")
    private int olderThanDays;

    @Scheduled(fixedDelayString = "${scheduler.reservation.update.delay-ms}")
    @Transactional
    public void expireOverdueReservations() {
        reservationService.setExpireStatusOnOverdueReservations(updateLimit);
    }

    @Scheduled(fixedDelayString = "${scheduler.reservation.delete.delay-ms}")
    @Transactional
    public void deleteExpiredReservations() {
        Duration expiredOlderThan = Duration.ofDays(olderThanDays);
        List<UUID> basketsIds = basketService.getExpiredBasketsIdsOlderThan(expiredOlderThan, deleteLimit);
        if (!basketsIds.isEmpty()) {
            reservationService.deleteExpiredReservationsByBasketsIds(basketsIds);
        }
    }
}
