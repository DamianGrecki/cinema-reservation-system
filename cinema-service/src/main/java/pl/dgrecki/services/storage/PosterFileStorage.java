package pl.dgrecki.services.storage;

import java.util.UUID;

public interface PosterFileStorage {

    String store(UUID movieId, String originalFileName, byte[] imageContent);

    byte[] load(String fileName);

    boolean exists(String fileName);

    void delete(String fileName);
}
