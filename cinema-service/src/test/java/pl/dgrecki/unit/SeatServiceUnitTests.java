package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Seat;
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
}
