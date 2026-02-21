package pl.dgrecki.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Basket;
import pl.dgrecki.models.entities.CinemaHall;
import pl.dgrecki.models.entities.HallRow;
import pl.dgrecki.models.responses.BasketResponse;
import pl.dgrecki.repositories.BasketRepository;
import pl.dgrecki.repositories.HallRowRepository;
import pl.dgrecki.repositories.ReservationRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static pl.dgrecki.constants.ExceptionMessages.BASKET_NOT_FOUND_MSG;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallRowService {


    private final HallRowRepository hallRowRepository;

    @Transactional
    public List<HallRow> getRowsByCinemaHallId(CinemaHall cinemaHall) {
        return hallRowRepository.findByCinemaHall(cinemaHall);
    }
}
