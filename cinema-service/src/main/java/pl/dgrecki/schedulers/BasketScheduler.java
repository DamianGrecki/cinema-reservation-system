package pl.dgrecki.schedulers;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.services.BasketService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasketScheduler {

    private final BasketService basketService;

    @Value("${scheduler.basket.delete.limit}")
    private int deleteLimit;

    @Value("${scheduler.basket.delete.older-than-days}")
    private int olderThanDays;

    @Scheduled(fixedDelayString = "${scheduler.basket.delete.delay-ms}")
    @Transactional
    public void deleteExpiredEmptyBaskets() {
        Duration expiredOlderThan = Duration.ofDays(olderThanDays);
        basketService.deleteExpiredBaskets(expiredOlderThan, deleteLimit);
    }
}
