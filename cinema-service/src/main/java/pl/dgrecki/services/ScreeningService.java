package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.SCREENING_NOT_FOUND_MSG;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Screening;
import pl.dgrecki.models.responses.screening.ScreeningListResponse;
import pl.dgrecki.models.responses.screening.ScreeningResponse;
import pl.dgrecki.repositories.ScreeningRepository;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepository screeningRepository;

    public ScreeningListResponse getScreeningsList() {
        List<Screening> screenings = screeningRepository.findAll();
        List<ScreeningResponse> screeningResponses = screenings.stream()
                .map(screening -> new ScreeningResponse(
                        screening.getId(),
                        screening.getCinemaHall().getName(),
                        screening.getMovie().getTitle(),
                        screening.getStartTime(),
                        screening.getEndTime()))
                .toList();
        return new ScreeningListResponse(screeningResponses);
    }

    public Screening getById(UUID id) {
        Optional<Screening> screeningOptional = screeningRepository.findById(id);
        if (screeningOptional.isEmpty()) {
            throw new ResourceNotFoundException(SCREENING_NOT_FOUND_MSG);
        }
        return screeningOptional.get();
    }
}
