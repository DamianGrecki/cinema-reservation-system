package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.SCREENING_NOT_FOUND_MSG;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Movie;
import pl.dgrecki.models.entities.MovieVersion;
import pl.dgrecki.models.entities.Screening;
import pl.dgrecki.models.responses.screening.ScreeningListResponse;
import pl.dgrecki.models.responses.screening.ScreeningResponse;
import pl.dgrecki.repositories.ScreeningRepository;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieService movieService;
    private final Clock clock;

    public ScreeningListResponse getScreeningsListByDate(LocalDate date) {
        Instant startOfDay = date.atStartOfDay(clock.getZone()).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        List<Screening> screenings = screeningRepository.findAllByDateWithDetails(startOfDay, endOfDay);
        return new ScreeningListResponse(mapToResponses(screenings));
    }

    private List<ScreeningResponse> mapToResponses(List<Screening> screenings) {
        return screenings.stream().map(this::mapToResponse).toList();
    }

    private ScreeningResponse mapToResponse(Screening screening) {
        MovieVersion movieVersion = screening.getMovieVersion();
        Movie movie = movieVersion.getMovie();
        return new ScreeningResponse(
                screening.getId(),
                screening.getCinemaHall().getName(),
                movie.getTitle(),
                movieService.buildPosterUrl(movie),
                movieVersion.getMovieFormat().toString(),
                movieVersion.getPresentationType(),
                movieVersion.getAudioLanguage(),
                screening.getStartTime(),
                screening.getEndTime());
    }

    public ScreeningResponse getScreeningResponseById(UUID id) {
        Screening screening = screeningRepository
                .findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SCREENING_NOT_FOUND_MSG, id)));
        return mapToResponse(screening);
    }

    public Screening getById(UUID id) {
        Optional<Screening> screeningOptional = screeningRepository.findById(id);
        if (screeningOptional.isEmpty()) {
            throw new ResourceNotFoundException(String.format(SCREENING_NOT_FOUND_MSG, id));
        }
        return screeningOptional.get();
    }
}
