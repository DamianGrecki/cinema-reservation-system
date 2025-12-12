package org.example.controllers;

import static org.example.constants.Endpoints.MOVIES_ENDPOINT;

import lombok.RequiredArgsConstructor;
import org.example.models.responses.movie.MovieListResponse;
import org.example.services.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
