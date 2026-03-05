package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.dgrecki.services.TicketDownloadTokenService;

class TicketDownloadTokenServiceUnitTests {

    private static final String SECRET = "testSecretKeyForHmacSigning";

    private TicketDownloadTokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TicketDownloadTokenService(SECRET);
    }

    @Test
    void generateUrlShouldContainTicketIdAndSignatureTest() {
        UUID ticketId = UUID.randomUUID();

        String url = tokenService.generateUrl(ticketId);

        assertTrue(url.startsWith("/api/ticket/" + ticketId + "/download?signature="));
    }

    @Test
    void isValidSignatureShouldReturnTrueForCorrectSignatureTest() {
        UUID ticketId = UUID.randomUUID();
        String url = tokenService.generateUrl(ticketId);
        String signature = url.substring(url.indexOf("signature=") + "signature=".length());

        assertTrue(tokenService.isValidSignature(ticketId, signature));
    }

    @Test
    void isValidSignatureShouldReturnFalseForWrongSignatureTest() {
        UUID ticketId = UUID.randomUUID();

        assertFalse(tokenService.isValidSignature(ticketId, "invalidSignature"));
    }

    @Test
    void isValidSignatureShouldReturnFalseForDifferentTicketIdTest() {
        UUID ticketId = UUID.randomUUID();
        UUID anotherTicketId = UUID.randomUUID();
        String url = tokenService.generateUrl(ticketId);
        String signature = url.substring(url.indexOf("signature=") + "signature=".length());

        assertFalse(tokenService.isValidSignature(anotherTicketId, signature));
    }

    @Test
    void generateUrlShouldProduceDeterministicSignatureForSameTicketIdTest() {
        UUID ticketId = UUID.randomUUID();

        String url1 = tokenService.generateUrl(ticketId);
        String url2 = tokenService.generateUrl(ticketId);

        assertEquals(url1, url2);
    }

    @Test
    void differentSecretsShouldProduceDifferentSignaturesTest() {
        UUID ticketId = UUID.randomUUID();
        TicketDownloadTokenService otherService = new TicketDownloadTokenService("differentSecret");

        String url1 = tokenService.generateUrl(ticketId);
        String url2 = otherService.generateUrl(ticketId);

        assertNotEquals(url1, url2);
    }
}
