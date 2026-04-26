package pl.dgrecki.services;

import static pl.dgrecki.constants.Endpoints.MOVIES_ENDPOINT;
import static pl.dgrecki.constants.ExceptionMessages.MOVIE_NOT_FOUND_MSG;
import static pl.dgrecki.constants.ExceptionMessages.POSTER_NOT_FOUND_MSG;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Movie;
import pl.dgrecki.models.responses.movie.MovieListResponse;
import pl.dgrecki.models.responses.movie.MovieResponse;
import pl.dgrecki.repositories.MovieRepository;
import pl.dgrecki.services.storage.PosterFileStorage;

@Service
@RequiredArgsConstructor
public class MovieService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    private final MovieRepository movieRepository;
    private final PosterFileStorage posterFileStorage;

    @Transactional(readOnly = true)
    public MovieListResponse getMoviesList() {
        List<Movie> movies = movieRepository.findAll();

        List<MovieResponse> movieResponses = movies.stream()
                .map(movie -> new MovieResponse(
                        movie.getId(), movie.getTitle(), movie.getDescription(), buildPosterUrl(movie)))
                .toList();

        return new MovieListResponse(movieResponses);
    }

    @Transactional
    public void uploadPoster(UUID movieId, MultipartFile file) {
        Movie movie = movieRepository
                .findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MOVIE_NOT_FOUND_MSG, movieId)));

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported image type. Allowed: JPEG, PNG, WebP");
        }

        if (movie.getPosterFileName() != null) {
            posterFileStorage.delete(movie.getPosterFileName());
        }

        try {
            String fileName = posterFileStorage.store(movieId, file.getOriginalFilename(), file.getBytes());
            movie.setPosterFileName(fileName);
            movieRepository.save(movie);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload poster", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] getPosterImage(UUID movieId) {
        Movie movie = movieRepository
                .findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MOVIE_NOT_FOUND_MSG, movieId)));

        if (movie.getPosterFileName() == null) {
            throw new ResourceNotFoundException(String.format(POSTER_NOT_FOUND_MSG, movieId));
        }

        return posterFileStorage.load(movie.getPosterFileName());
    }

    @Transactional(readOnly = true)
    public String getPosterFileName(UUID movieId) {
        Movie movie = movieRepository
                .findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(MOVIE_NOT_FOUND_MSG, movieId)));
        return movie.getPosterFileName();
    }

    public String buildPosterUrl(Movie movie) {
        if (movie.getPosterFileName() == null) {
            return null;
        }
        return MOVIES_ENDPOINT + "/" + movie.getId() + "/poster";
    }
}
