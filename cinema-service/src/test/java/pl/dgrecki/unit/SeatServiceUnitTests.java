package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.SeatStatusDto;
import pl.dgrecki.models.entities.Seat;
import pl.dgrecki.models.responses.RowSeatsMapResponse;
import pl.dgrecki.repositories.RowWithSeatAndStatus;
import pl.dgrecki.repositories.SeatRepository;
import pl.dgrecki.services.SeatService;

@ExtendWith(MockitoExtension.class)
class SeatServiceUnitTests {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatService seatService;

    @Test
    void getByIdWhenExistsShouldReturnSeatTest() {
        UUID id = UUID.randomUUID();
        Seat seat = new Seat();
        seat.setId(id);

        when(seatRepository.findById(id)).thenReturn(Optional.of(seat));

        Seat result = seatService.getById(id);

        assertEquals(id, result.getId());
        verify(seatRepository, times(1)).findById(id);
    }

    @Test
    void getByIdWhenNotExistsShouldThrowExceptionTest() {
        UUID id = UUID.randomUUID();

        when(seatRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> seatService.getById(id));

        verify(seatRepository, times(1)).findById(id);
    }

    @Test
    void getRowsSeatsMapByScreeningIdTest() {
        UUID screeningId = UUID.randomUUID();
        UUID seatId1 = UUID.randomUUID();
        UUID seatId2 = UUID.randomUUID();

        RowWithSeatAndStatus row1seat1 = mock(RowWithSeatAndStatus.class);
        when(row1seat1.getRowNumber()).thenReturn(1);
        when(row1seat1.getSeatNumber()).thenReturn(1);
        when(row1seat1.getSeatId()).thenReturn(seatId1);
        when(row1seat1.getReserved()).thenReturn(false);

        RowWithSeatAndStatus row1seat2 = mock(RowWithSeatAndStatus.class);
        when(row1seat2.getRowNumber()).thenReturn(1);
        when(row1seat2.getSeatNumber()).thenReturn(2);
        when(row1seat2.getSeatId()).thenReturn(seatId2);
        when(row1seat2.getReserved()).thenReturn(true);

        when(seatRepository.findSeatsWithRowAndStatuses(eq(screeningId), anySet()))
                .thenReturn(List.of(row1seat1, row1seat2));

        List<RowSeatsMapResponse> result = seatService.getRowsSeatsMapByScreeningId(screeningId);

        assertEquals(1, result.size());

        RowSeatsMapResponse row = result.getFirst();
        assertEquals(1, row.rowNumber());
        assertEquals(2, row.seats().size());

        SeatStatusDto first = row.seats().getFirst();
        assertEquals(1, first.seatNumber());
        assertEquals(seatId1, first.seatId());
        assertFalse(first.reserved());

        SeatStatusDto second = row.seats().getLast();
        assertEquals(2, second.seatNumber());
        assertEquals(seatId2, second.seatId());
        assertTrue(second.reserved());

        verify(seatRepository, times(1)).findSeatsWithRowAndStatuses(eq(screeningId), anySet());
    }
}
