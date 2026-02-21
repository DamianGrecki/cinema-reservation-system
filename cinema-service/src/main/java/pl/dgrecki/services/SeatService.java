package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.SEAT_NOT_FOUND_MSG;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.CinemaHall;
import pl.dgrecki.models.entities.HallRow;
import pl.dgrecki.models.entities.Screening;
import pl.dgrecki.models.entities.Seat;
import pl.dgrecki.models.responses.RowSeatsMapResponse;
import pl.dgrecki.repositories.SeatRepository;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreeningService screeningService;
    private final HallRowService hallRowService;

    public Seat getById(UUID id) {
        Optional<Seat> seatOptional = seatRepository.findById(id);
        if (seatOptional.isEmpty()) {
            throw new ResourceNotFoundException(String.format(SEAT_NOT_FOUND_MSG, id));
        }
        return seatOptional.get();
    }

    public List<RowSeatsMapResponse> getRowSeatsMapByScreeningId(UUID screeningId) {
            Screening screening = screeningService.getById(screeningId);
            CinemaHall cinemaHall = screening.getCinemaHall();
            List<HallRow> hallRows = hallRowService.getRowsByCinemaHallId(cinemaHall);
            List<RowSeatsMapResponse> response = new ArrayList<>();
            for(HallRow row : hallRows){
                List<UUID> seatsIds = seatRepository.findIdsByHallRow(row);
                response.add(new RowSeatsMapResponse(row.getRowNumber(), seatsIds));
            }
            return response;
    }
}
