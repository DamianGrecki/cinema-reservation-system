package pl.dgrecki.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.MailTemplate;
import pl.dgrecki.models.enums.TemplateType;
import pl.dgrecki.repositories.MailTemplateRepository;
import pl.dgrecki.services.MailTemplateService;

@ExtendWith(MockitoExtension.class)
class MailTemplateServiceUnitTests {

    @Mock
    private MailTemplateRepository mailTemplateRepository;

    @InjectMocks
    private MailTemplateService mailTemplateService;

    @Test
    void shouldReturnActiveUserRegistrationTemplateTest() {
        MailTemplate template = MailTemplate.builder()
                .templateName("user-registration")
                .templateKeys(Set.of("email", "name"))
                .build();
        when(mailTemplateRepository.findByTemplateTypeAndIsActiveTrue(TemplateType.USER_REGISTRATION))
                .thenReturn(Optional.of(template));

        MailTemplate result = mailTemplateService.getUserRegistrationMailTemplate();

        assertEquals("user-registration", result.getTemplateName());
        assertEquals(Set.of("email", "name"), result.getTemplateKeys());
    }

    @Test
    void shouldThrowWhenNoActiveUserRegistrationTemplateTest() {
        when(mailTemplateRepository.findByTemplateTypeAndIsActiveTrue(TemplateType.USER_REGISTRATION))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class, () -> mailTemplateService.getUserRegistrationMailTemplate());

        assertEquals("Not found active template type: USER_REGISTRATION", ex.getMessage());
    }

    @Test
    void shouldValidateTemplateVariablesSuccessfullyTest() {
        MailTemplate template = MailTemplate.builder()
                .templateName("user-registration")
                .templateKeys(Set.of("email", "name"))
                .build();

        Map<String, Object> variables = Map.of(
                "email", "test@example.com",
                "name", "John");

        assertDoesNotThrow(() -> mailTemplateService.validateTemplateVariables(template, variables));
    }

    @Test
    void shouldThrowWhenMissingTemplateVariablesTest() {
        MailTemplate template = MailTemplate.builder()
                .templateName("user-registration")
                .templateKeys(Set.of("email", "name", "age"))
                .build();

        Map<String, Object> variables = Map.of(
                "email", "test@example.com",
                "name", "John");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mailTemplateService.validateTemplateVariables(template, variables));

        assertEquals("Missing variables: [age] for template: user-registration", ex.getMessage());
    }
}
