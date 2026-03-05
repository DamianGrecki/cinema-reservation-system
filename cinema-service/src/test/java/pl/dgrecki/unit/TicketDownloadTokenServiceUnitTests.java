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
    void generateUrlShouldContainOrderIdAndSignatureTest() {
        UUID orderId = UUID.randomUUID();

        String url = tokenService.generateUrl(orderId);

        assertTrue(url.startsWith("/api/orders/" + orderId + "/tickets/download?signature="));
    }

    @Test
    void isValidSignatureShouldReturnTrueForCorrectSignatureTest() {
        UUID orderId = UUID.randomUUID();
        String url = tokenService.generateUrl(orderId);
        String signature = url.substring(url.indexOf("signature=") + "signature=".length());

        assertTrue(tokenService.isValidSignature(orderId, signature));
    }

    @Test
    void isValidSignatureShouldReturnFalseForWrongSignatureTest() {
        UUID orderId = UUID.randomUUID();

        assertFalse(tokenService.isValidSignature(orderId, "invalidSignature"));
    }

    @Test
    void isValidSignatureShouldReturnFalseForDifferentOrderIdTest() {
        UUID orderId = UUID.randomUUID();
        UUID anotherOrderId = UUID.randomUUID();
        String url = tokenService.generateUrl(orderId);
        String signature = url.substring(url.indexOf("signature=") + "signature=".length());

        assertFalse(tokenService.isValidSignature(anotherOrderId, signature));
    }

    @Test
    void generateUrlShouldProduceDeterministicSignatureForSameOrderIdTest() {
        UUID orderId = UUID.randomUUID();

        String url1 = tokenService.generateUrl(orderId);
        String url2 = tokenService.generateUrl(orderId);

        assertEquals(url1, url2);
    }

    @Test
    void differentSecretsShouldProduceDifferentSignaturesTest() {
        UUID orderId = UUID.randomUUID();
        TicketDownloadTokenService otherService = new TicketDownloadTokenService("differentSecret");

        String url1 = tokenService.generateUrl(orderId);
        String url2 = otherService.generateUrl(orderId);

        assertNotEquals(url1, url2);
    }
}
