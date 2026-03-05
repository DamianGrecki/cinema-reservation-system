package pl.dgrecki.services.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "ticket.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalTicketFileStorage implements TicketFileStorage {

    private final Path storageDir;

    public LocalTicketFileStorage(@Value("${ticket.storage.local.directory}") String directory) {
        this.storageDir = Path.of(directory);
        try {
            Files.createDirectories(storageDir);
            log.info("Ticket storage directory: {}", storageDir.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create ticket storage directory: " + directory, e);
        }
    }

    @Override
    public String store(UUID ticketId, byte[] pdfContent) {
        String fileName = ticketId + "_ticket.pdf";
        Path target = storageDir.resolve(fileName);
        try {
            Files.write(target, pdfContent);
            log.debug("Stored ticket PDF: {}", target);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store ticket PDF: " + fileName, e);
        }
    }

    @Override
    public byte[] load(String fileName) {
        Path target = storageDir.resolve(fileName);
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ticket PDF: " + fileName, e);
        }
    }
}
