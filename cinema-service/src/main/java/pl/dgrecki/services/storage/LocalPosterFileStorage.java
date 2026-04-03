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
@ConditionalOnProperty(name = "poster.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalPosterFileStorage implements PosterFileStorage {

    private final Path storageDir;

    public LocalPosterFileStorage(@Value("${poster.storage.local.directory}") String directory) {
        this.storageDir = Path.of(directory);
        try {
            Files.createDirectories(storageDir);
            log.info("Poster storage directory: {}", storageDir.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create poster storage directory: " + directory, e);
        }
    }

    @Override
    public String store(UUID movieId, String originalFileName, byte[] imageContent) {
        String extension = extractExtension(originalFileName);
        String fileName = movieId + "_poster" + extension;
        Path target = storageDir.resolve(fileName);
        try {
            Files.write(target, imageContent);
            log.debug("Stored poster: {}", target);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store poster: " + fileName, e);
        }
    }

    @Override
    public byte[] load(String fileName) {
        Path target = storageDir.resolve(fileName);
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load poster: " + fileName, e);
        }
    }

    @Override
    public boolean exists(String fileName) {
        return Files.exists(storageDir.resolve(fileName));
    }

    @Override
    public void delete(String fileName) {
        Path target = storageDir.resolve(fileName);
        try {
            Files.deleteIfExists(target);
            log.debug("Deleted poster: {}", target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete poster: " + fileName, e);
        }
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex);
    }
}
