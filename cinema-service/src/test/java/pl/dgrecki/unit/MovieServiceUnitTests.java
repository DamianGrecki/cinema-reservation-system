package pl.dgrecki.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dgrecki.models.entities.Movie;
import pl.dgrecki.models.responses.movie.MovieListResponse;
import pl.dgrecki.models.responses.movie.MovieResponse;
import pl.dgrecki.repositories.MovieRepository;
import pl.dgrecki.services.MovieService;

@ExtendWith(MockitoExtension.class)
class MovieServiceUnitTests {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    void getMoviesListTest() {
        Movie movie1 = new Movie(UUID.randomUUID(), "Movie 1", "Description 1", Instant.now());
        Movie movie2 = new Movie(UUID.randomUUID(), "Movie 2", "Description 2", Instant.now());

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
