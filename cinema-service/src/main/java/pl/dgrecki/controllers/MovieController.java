package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.MOVIES_ENDPOINT;

import java.net.URLConnection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.dgrecki.models.responses.movie.MovieListResponse;
import pl.dgrecki.services.MovieService;

@RestController
@RequestMapping(MOVIES_ENDPOINT)
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<MovieListResponse> getMovies() {
        return ResponseEntity.ok(movieService.getMoviesList());
    }

    @PostMapping("/{movieId}/poster")
    public ResponseEntity<Void> uploadPoster(@PathVariable UUID movieId, @RequestParam("file") MultipartFile file) {
        movieService.uploadPoster(movieId, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{movieId}/poster")
    public ResponseEntity<byte[]> getPoster(@PathVariable UUID movieId) {
        byte[] image = movieService.getPosterImage(movieId);
        String fileName = movieService.getPosterFileName(movieId);
        String contentType = URLConnection.guessContentTypeFromName(fileName);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(image);
    }
}
