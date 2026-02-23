package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.dgrecki.constants.ExceptionMessages.TICKET_TEMPLATE_NOT_FOUND_MSG;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.entities.TicketTemplate;
import pl.dgrecki.repositories.TicketTemplateRepository;
import pl.dgrecki.services.TicketTemplateService;

@ExtendWith(MockitoExtension.class)
class TicketTemplateServiceUnitTests {

    @Mock
    private TicketTemplateRepository ticketTemplateRepository;

    @InjectMocks
    private TicketTemplateService ticketTemplateService;

    @Test
    void getActiveTemplateWhenExistsShouldReturnTemplateTest() {
        TicketTemplate template = new TicketTemplate();
        template.setId(1L);
        template.setActive(true);

        when(ticketTemplateRepository.findTicketTemplateByIsActive(true)).thenReturn(Optional.of(template));

        TicketTemplate result = ticketTemplateService.getActiveTemplate();

        assertNotNull(result);
        assertEquals(template.getId(), result.getId());

        verify(ticketTemplateRepository, times(1)).findTicketTemplateByIsActive(true);
    }

    @Test
    void getActiveTemplateWhenNotExistsShouldThrowExceptionTest() {
        when(ticketTemplateRepository.findTicketTemplateByIsActive(true)).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> ticketTemplateService.getActiveTemplate());

        assertEquals(TICKET_TEMPLATE_NOT_FOUND_MSG, exception.getMessage());

        verify(ticketTemplateRepository, times(1)).findTicketTemplateByIsActive(true);
    }
}
