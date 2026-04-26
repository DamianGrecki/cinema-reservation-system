package pl.dgrecki.services;

import static pl.dgrecki.constants.ExceptionMessages.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.TicketTemplate;
import pl.dgrecki.repositories.TicketTemplateRepository;

@Service
@RequiredArgsConstructor
public class TicketTemplateService {

    private final TicketTemplateRepository ticketTemplateRepository;

    @Transactional(readOnly = true)
    public TicketTemplate getActiveTemplate() {
        return ticketTemplateRepository
                .findTicketTemplateByIsActive(true)
                .orElseThrow(() -> new RuntimeException(TICKET_TEMPLATE_NOT_FOUND_MSG));
    }
}
