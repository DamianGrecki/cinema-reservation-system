package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.BASKET_NOT_FOUND_MSG;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Basket;
import pl.dgrecki.models.responses.BasketResponse;
import pl.dgrecki.repositories.BasketRepository;
import pl.dgrecki.repositories.ReservationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketService {

    private static final Duration EXPIRE_DURATION = Duration.ofMinutes(15);

    private final BasketRepository basketRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    @Transactional
    public BasketResponse addBasket() {
        Instant now = clock.instant();
        Basket basket = Basket.builder()
                .expiresAt(now.plus(EXPIRE_DURATION))
                .createdAt(now)
                .build();
        Basket savedBasket = basketRepository.save(basket);
        return new BasketResponse(savedBasket.getId());
    }

    @Transactional
    public void deleteExpiredBaskets(Duration expiredDuration, int limit) {
        Instant expireOlderThanDate = clock.instant().minus(expiredDuration);
        List<UUID> ids = getExpiredBasketsIdsWithoutReservations(expireOlderThanDate, limit);
        if (!ids.isEmpty()) {
            int deletedCount = basketRepository.deleteExpiredBasketsWithReservations(ids, expireOlderThanDate);
            if (deletedCount > 0) {
                log.info("Deleted {} expired baskets", deletedCount);
            }
        }
    }

    @Transactional
    public Basket getById(UUID id) {
        Optional<Basket> basketOptional = basketRepository.findById(id);
        if (basketOptional.isEmpty()) {
            throw new ResourceNotFoundException(String.format(BASKET_NOT_FOUND_MSG, id));
        }
        return basketOptional.get();
    }

    @Transactional
    public List<UUID> getExpiredBasketsIds(int limit) {
        return getBasketsIdsOlderThanExpiredAt(clock.instant(), limit);
    }

    @Transactional
    public List<UUID> getExpiredBasketsIdsOlderThan(Duration expiredDuration, int limit) {
        Instant expireOlderThanDate = clock.instant().minus(expiredDuration);
        return getBasketsIdsOlderThanExpiredAt(expireOlderThanDate, limit);
    }

    private List<UUID> getExpiredBasketsIdsWithoutReservations(Instant expireAt, int limit) {
        return reservationRepository.findExpiredBasketsIdsWithoutReservations(expireAt, Pageable.ofSize(limit));
    }

    private List<UUID> getBasketsIdsOlderThanExpiredAt(Instant expireAt, int limit) {
        return basketRepository.findIdsByExpiresAtBefore(expireAt, Pageable.ofSize(limit));
    }
}
