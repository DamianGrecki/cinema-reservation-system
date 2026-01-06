package pl.dgrecki.unit.user.activation;

import static org.junit.jupiter.api.Assertions.*;
import static pl.dgrecki.constants.Endpoints.USER_ACTIVATE_ENDPOINT;

import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.dgrecki.services.user.activation.ActivationLinkService;

class ActivationLinkServiceUnitTests {

    private static final String APP_URL = "https://example.com";

    @ParameterizedTest
    @MethodSource("appUrlProvider")
    void shouldBuildActivationLinkTest(String appUrl) {
        UUID token = UUID.randomUUID();
        ActivationLinkService service = new ActivationLinkService(appUrl);
        String link = service.buildActivationLink(token);
        String expectedLink = APP_URL + USER_ACTIVATE_ENDPOINT + "?token=" + token;
        assertEquals(expectedLink, link);
    }

    private static Stream<Arguments> appUrlProvider() {
        return Stream.of(Arguments.of("https://example.com"), Arguments.of("https://example.com/"));
    }
}
