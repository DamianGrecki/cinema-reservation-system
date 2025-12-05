package org.example.controllers;

import lombok.RequiredArgsConstructor;
import org.example.models.responses.movie.MovieListResponse;
import org.example.services.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<MovieListResponse> getMovies() {
        return ResponseEntity.ok(movieService.getMoviesList());
    }
}
