package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.SEAT_NOT_FOUND_MSG;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Seat;
import pl.dgrecki.repositories.SeatRepository;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    public Seat getById(UUID id) {
        Optional<Seat> seatOptional = seatRepository.findById(id);
        if (seatOptional.isEmpty()) {
            throw new ResourceNotFoundException(SEAT_NOT_FOUND_MSG);
        }
        return seatOptional.get();
    }
}
