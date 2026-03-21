package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.models.enums.AudioLanguage.ENGLISH;
import static pl.dgrecki.models.enums.MovieFormat.FORMAT_2D;
import static pl.dgrecki.models.enums.PresentationType.ORIGINAL_WITH_SUBTITLES;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.*;
import pl.dgrecki.models.responses.screening.ScreeningListResponse;
import pl.dgrecki.models.responses.screening.ScreeningResponse;
import pl.dgrecki.repositories.ScreeningRepository;
import pl.dgrecki.services.ScreeningService;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceUnitTests {

    @Mock
    private ScreeningRepository screeningRepository;

    @InjectMocks
    private ScreeningService screeningService;

    @Test
    void getScreeningsListShouldReturnMappedResponsesTest() {
        UUID screeningId = UUID.randomUUID();

        Movie movie = new Movie();
        movie.setTitle("Interstellar");

        MovieVersion movieVersion = new MovieVersion();
        movieVersion.setMovie(movie);
        movieVersion.setMovieFormat(FORMAT_2D);
        movieVersion.setPresentationType(ORIGINAL_WITH_SUBTITLES);
        movieVersion.setAudioLanguage(ENGLISH);

        CinemaHall hall = new CinemaHall();
        hall.setName("Hall 1");

        Instant start = Instant.parse("2026-01-01T18:00:00Z");
        Instant end = Instant.parse("2026-01-01T20:30:00Z");

        Screening screening = Screening.builder()
                .id(screeningId)
                .movieVersion(movieVersion)
                .cinemaHall(hall)
                .startTime(start)
                .endTime(end)
                .build();

        when(screeningRepository.findAllWithDetails()).thenReturn(List.of(screening));

        ScreeningListResponse response = screeningService.getScreeningsList();

        assertNotNull(response);
        assertEquals(1, response.getScreenings().size());

        ScreeningResponse screeningResponse = response.getScreenings().getFirst();

        assertEquals(screeningId, screeningResponse.getId());
        assertEquals("Hall 1", screeningResponse.getCinemaHallName());
        assertEquals("Interstellar", screeningResponse.getMovieTitle());
        assertEquals(FORMAT_2D.toString(), screeningResponse.getMovieFormat());
        assertEquals(ORIGINAL_WITH_SUBTITLES, screeningResponse.getPresentationType());
        assertEquals(ENGLISH, screeningResponse.getAudioLanguage());
        assertEquals(start, screeningResponse.getStartTime());
        assertEquals(end, screeningResponse.getEndTime());

        verify(screeningRepository, times(1)).findAllWithDetails();
    }

    @Test
    void getScreeningsListWhenEmptyShouldReturnEmptyListTest() {
        when(screeningRepository.findAllWithDetails()).thenReturn(List.of());

        ScreeningListResponse response = screeningService.getScreeningsList();

        assertNotNull(response);
        assertTrue(response.getScreenings().isEmpty());

        verify(screeningRepository, times(1)).findAllWithDetails();
    }

    @Test
    void getByIdWhenExistsShouldReturnScreeningTest() {
        UUID id = UUID.randomUUID();
        Screening screening = new Screening();
        screening.setId(id);

        when(screeningRepository.findById(id)).thenReturn(Optional.of(screening));

        Screening result = screeningService.getById(id);

        assertEquals(id, result.getId());
        verify(screeningRepository, times(1)).findById(id);
    }

    @Test
    void getByIdWhenNotExistsShouldThrowExceptionTest() {
        UUID id = UUID.randomUUID();

        when(screeningRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> screeningService.getById(id));

        verify(screeningRepository, times(1)).findById(id);
    }
}
