package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.models.enums.AudioLanguage.ENGLISH;
import static pl.dgrecki.models.enums.MovieFormat.FORMAT_2D;
import static pl.dgrecki.models.enums.PresentationType.ORIGINAL_WITH_SUBTITLES;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
import pl.dgrecki.services.MovieService;
import pl.dgrecki.services.ScreeningService;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceUnitTests {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Warsaw");

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private MovieService movieService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ScreeningService screeningService;

    @Test
    void getScreeningsListByDateShouldReturnFilteredResponsesTest() {
        UUID screeningId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 1, 1);
        Instant startOfDay = date.atStartOfDay(ZONE_ID).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

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

        when(clock.getZone()).thenReturn(ZONE_ID);
        when(screeningRepository.findAllByDateWithDetails(startOfDay, endOfDay)).thenReturn(List.of(screening));

        ScreeningListResponse response = screeningService.getScreeningsListByDate(date);

        assertNotNull(response);
        assertEquals(1, response.getScreenings().size());

        ScreeningResponse screeningResponse = response.getScreenings().getFirst();

        assertEquals(screeningId, screeningResponse.getId());
        assertEquals("Interstellar", screeningResponse.getMovieTitle());
        assertEquals(start, screeningResponse.getStartTime());

        verify(screeningRepository, times(1)).findAllByDateWithDetails(startOfDay, endOfDay);
    }

    @Test
    void getScreeningsListByDateWhenEmptyShouldReturnEmptyListTest() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        Instant startOfDay = date.atStartOfDay(ZONE_ID).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

        when(clock.getZone()).thenReturn(ZONE_ID);
        when(screeningRepository.findAllByDateWithDetails(startOfDay, endOfDay)).thenReturn(List.of());

        ScreeningListResponse response = screeningService.getScreeningsListByDate(date);

        assertNotNull(response);
        assertTrue(response.getScreenings().isEmpty());

        verify(screeningRepository, times(1)).findAllByDateWithDetails(startOfDay, endOfDay);
    }

    @Test
    void getScreeningResponseByIdWhenExistsShouldReturnMappedResponseTest() {
        UUID id = UUID.randomUUID();

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
                .id(id)
                .movieVersion(movieVersion)
                .cinemaHall(hall)
                .startTime(start)
                .endTime(end)
                .build();

        when(screeningRepository.findByIdWithDetails(id)).thenReturn(Optional.of(screening));

        ScreeningResponse response = screeningService.getScreeningResponseById(id);

        assertEquals(id, response.getId());
        assertEquals("Hall 1", response.getCinemaHallName());
        assertEquals("Interstellar", response.getMovieTitle());
        assertEquals(start, response.getStartTime());
        assertEquals(end, response.getEndTime());

        verify(screeningRepository, times(1)).findByIdWithDetails(id);
    }

    @Test
    void getScreeningResponseByIdWhenNotExistsShouldThrowExceptionTest() {
        UUID id = UUID.randomUUID();

        when(screeningRepository.findByIdWithDetails(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> screeningService.getScreeningResponseById(id));

        verify(screeningRepository, times(1)).findByIdWithDetails(id);
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
