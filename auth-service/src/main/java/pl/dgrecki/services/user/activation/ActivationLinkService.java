package pl.dgrecki.services.user.activation;

import static pl.dgrecki.constants.Endpoints.USER_ACTIVATE_ENDPOINT;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ActivationLinkService {

    private final String appUrl;

    public ActivationLinkService(@Value("${app.url}") String appUrl) {
        this.appUrl = appUrl;
    }

    @Transactional
    public String buildActivationLink(UUID activationToken) {
        return UriComponentsBuilder.fromUriString(appUrl)
                .path(USER_ACTIVATE_ENDPOINT)
                .queryParam("token", activationToken)
                .build()
                .toUriString();
    }
}
