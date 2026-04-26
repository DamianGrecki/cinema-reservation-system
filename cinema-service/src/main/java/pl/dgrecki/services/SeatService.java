package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.SEAT_NOT_FOUND_MSG;
import static pl.dgrecki.models.enums.ReservationStatus.*;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.SeatStatusDto;
import pl.dgrecki.models.entities.Seat;
import pl.dgrecki.models.enums.ReservationStatus;
import pl.dgrecki.models.responses.RowSeatsMapResponse;
import pl.dgrecki.repositories.RowWithSeatAndStatus;
import pl.dgrecki.repositories.SeatRepository;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public Seat getById(UUID id) {
        Optional<Seat> seatOptional = seatRepository.findById(id);
        if (seatOptional.isEmpty()) {
            throw new ResourceNotFoundException(String.format(SEAT_NOT_FOUND_MSG, id));
        }
        return seatOptional.get();
    }

    @Transactional(readOnly = true)
    public List<RowSeatsMapResponse> getRowsSeatsMapByScreeningId(UUID screeningId) {
        List<RowWithSeatAndStatus> seats =
                seatRepository.findSeatsWithRowAndStatuses(screeningId, ReservationStatus.getStatusesBlockingSeat());

        Map<Integer, List<SeatStatusDto>> grouped = seats.stream()
                .collect(Collectors.groupingBy(
                        RowWithSeatAndStatus::getRowNumber,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                e -> new SeatStatusDto(e.getSeatNumber(), e.getSeatId(), e.getReserved()),
                                Collectors.toList())));

        return grouped.entrySet().stream()
                .map(e -> new RowSeatsMapResponse(e.getKey(), e.getValue()))
                .toList();
    }
}
