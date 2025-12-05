package org.example.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.models.Movie;
import org.example.models.responses.movie.MovieListResponse;
import org.example.models.responses.movie.MovieResponse;
import org.example.repositories.MovieRepository;
import org.example.services.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MovieServiceUnitTests {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMoviesListTest() {
        Movie movie1 = new Movie("Movie 1", "Description 1");
        Movie movie2 = new Movie("Movie 2", "Description 2");

        when(movieRepository.findAll()).thenReturn(List.of(movie1, movie2));

        List<MovieResponse> moviesList = movieService.getMoviesList().getMovies();

        assertEquals(2, moviesList.size());
        assertEquals(movie1.getTitle(), moviesList.getFirst().getTitle());
        assertEquals(movie1.getDescription(), moviesList.getFirst().getDescription());

        assertEquals(movie2.getTitle(), moviesList.getLast().getTitle());
        assertEquals(movie2.getDescription(), moviesList.getLast().getDescription());
    }

    @Test
    void getEmptyMoviesListTest() {
        when(movieRepository.findAll()).thenReturn(List.of());

        MovieListResponse response = movieService.getMoviesList();

        assertEquals(0, response.getMovies().size());
    }
}
