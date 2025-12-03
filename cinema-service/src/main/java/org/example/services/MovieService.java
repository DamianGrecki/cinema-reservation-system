package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.models.Movie;
import org.example.models.responses.movie.MovieListResponse;
import org.example.models.responses.movie.MovieResponse;
import org.example.repositories.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public MovieListResponse getMoviesList() {
        List<Movie> movies = movieRepository.findAll();

        List<MovieResponse> movieResponses = movies.stream()
                .map(movie -> new MovieResponse(
                        movie.getId(),
                        movie.getTitle(),
                        movie.getDescription()
                ))
                .toList();

        return new MovieListResponse(movieResponses);
    }
}