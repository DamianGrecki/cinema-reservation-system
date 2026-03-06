package pl.dgrecki.services;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.MailTemplate;
import pl.dgrecki.models.enums.TemplateType;
import pl.dgrecki.repositories.MailTemplateRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailTemplateService {

    private final MailTemplateRepository mailTemplateRepository;

    public MailTemplate getMailTemplate(TemplateType templateType) {
        return mailTemplateRepository
                .findByTemplateTypeAndIsActiveTrue(templateType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Not found active template type: %s", templateType)));
    }

    public void validateTemplateVariables(MailTemplate template, Map<String, Object> variables) {
        Set<String> requiredKeys = template.getTemplateKeys();

        Set<String> missingKeys =
                requiredKeys.stream().filter(k -> variables.get(k) == null).collect(Collectors.toSet());

        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Missing variables: %s for template: %s", missingKeys, template.getTemplateName()));
        }
    }
}
