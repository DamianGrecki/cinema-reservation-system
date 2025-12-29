package pl.dgrecki.controllers;

import static pl.dgrecki.constants.Endpoints.MOVIES_ENDPOINT;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dgrecki.models.responses.movie.MovieListResponse;
import pl.dgrecki.services.MovieService;

@RestController
@RequestMapping(MOVIES_ENDPOINT)
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<MovieListResponse> getMovies() {
        return ResponseEntity.ok(movieService.getMoviesList());
    }
}
