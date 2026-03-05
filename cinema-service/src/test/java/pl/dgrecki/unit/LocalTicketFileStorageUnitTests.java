package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.dgrecki.services.storage.LocalTicketFileStorage;

class LocalTicketFileStorageUnitTests {

    @TempDir
    Path tempDir;

    private LocalTicketFileStorage storage;

    @BeforeEach
    void setUp() {
        storage = new LocalTicketFileStorage(tempDir.toString());
    }

    @Test
    void storeShouldWriteFileAndReturnFileNameTest() {
        UUID ticketId = UUID.randomUUID();
        byte[] content = "pdf-content".getBytes();

        String fileName = storage.store(ticketId, content);

        assertEquals(ticketId + "_ticket.pdf", fileName);
        assertTrue(Files.exists(tempDir.resolve(fileName)));
    }

    @Test
    void loadShouldReturnStoredContentTest() {
        UUID ticketId = UUID.randomUUID();
        byte[] content = "pdf-content".getBytes();
        String fileName = storage.store(ticketId, content);

        byte[] loaded = storage.load(fileName);

        assertArrayEquals(content, loaded);
    }

    @Test
    void loadShouldThrowExceptionWhenFileDoesNotExistTest() {
        assertThrows(RuntimeException.class, () -> storage.load("nonexistent.pdf"));
    }

    @Test
    void storeShouldOverwriteExistingFileTest() throws IOException {
        UUID ticketId = UUID.randomUUID();
        byte[] firstContent = "first".getBytes();
        byte[] secondContent = "second".getBytes();

        String fileName = storage.store(ticketId, firstContent);
        storage.store(ticketId, secondContent);

        byte[] loaded = Files.readAllBytes(tempDir.resolve(fileName));
        assertArrayEquals(secondContent, loaded);
    }

    @Test
    void constructorShouldCreateDirectoryIfNotExistsTest() {
        Path newDir = tempDir.resolve("nested/subdir");

        new LocalTicketFileStorage(newDir.toString());

        assertTrue(Files.exists(newDir));
    }
}
