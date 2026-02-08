package pl.dgrecki.schedulers;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.dgrecki.services.ReservationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    @Value("${scheduler.reservation.update.limit}")
    private int updateLimit;

    @Value("${scheduler.reservation.delete.limit}")
    private int deleteLimit;

    @Value("${scheduler.reservation.delete.older-than-days}")
    private int olderThanDays;

    private final ReservationService reservationService;

    @Scheduled(fixedDelayString = "${scheduler.reservation.update.delay-ms}")
    public void expireOverdueReservations() {
        reservationService.setExpireStatusOnOverdueReservations(updateLimit);
    }

    @Scheduled(fixedDelayString = "${scheduler.reservation.delete.delay-ms}")
    public void deleteExpiredReservations() {
        Duration expiredOlderThan = Duration.ofDays(olderThanDays);
        reservationService.deleteExpiredReservationsOlderThan(expiredOlderThan, deleteLimit);
    }
}
