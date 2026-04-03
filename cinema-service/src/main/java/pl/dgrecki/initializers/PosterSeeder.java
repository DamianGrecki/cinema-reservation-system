package pl.dgrecki.initializers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import pl.dgrecki.services.storage.PosterFileStorage;

@Slf4j
@Component
@RequiredArgsConstructor
public class PosterSeeder implements ApplicationRunner {

    private static final Map<UUID, String> MOVIE_POSTERS = Map.of(
            UUID.fromString("11111111-1111-1111-1111-111111111111"), "inception.jpg",
            UUID.fromString("22222222-2222-2222-2222-222222222222"), "the_matrix.jpg",
            UUID.fromString("33333333-3333-3333-3333-333333333333"), "interstellar.jpg",
            UUID.fromString("44444444-4444-4444-4444-444444444444"), "the_dark_knight.jpg",
            UUID.fromString("55555555-5555-5555-5555-555555555555"), "pulp_fiction.jpg",
            UUID.fromString("66666666-6666-6666-6666-666666666666"), "the_shawshank_redemption.jpg",
            UUID.fromString("77777777-7777-7777-7777-777777777777"), "gladiator.jpg",
            UUID.fromString("88888888-8888-8888-8888-888888888888"), "blade_runner_2049.jpg");

    private final PosterFileStorage posterFileStorage;

    @Override
    public void run(ApplicationArguments args) {
        MOVIE_POSTERS.forEach((movieId, posterFile) -> {
            String storedFileName = movieId + "_poster.jpg";
            if (!posterFileStorage.exists(storedFileName)) {
                copyPosterToStorage(movieId, posterFile);
            }
        });
    }

    private void copyPosterToStorage(UUID movieId, String posterFile) {
        try {
            ClassPathResource resource = new ClassPathResource("posters/" + posterFile);
            try (InputStream is = resource.getInputStream()) {
                byte[] content = is.readAllBytes();
                posterFileStorage.store(movieId, posterFile, content);
                log.info("Seeded poster to storage: {}", posterFile);
            }
        } catch (IOException e) {
            log.warn("Failed to seed poster {}: {}", posterFile, e.getMessage());
        }
    }
}
