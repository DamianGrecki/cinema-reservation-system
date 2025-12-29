package pl.dgrecki.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.Movie;
import pl.dgrecki.models.responses.movie.MovieListResponse;
import pl.dgrecki.models.responses.movie.MovieResponse;
import pl.dgrecki.repositories.MovieRepository;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public MovieListResponse getMoviesList() {
        List<Movie> movies = movieRepository.findAll();

        List<MovieResponse> movieResponses = movies.stream()
                .map(movie -> new MovieResponse(movie.getId(), movie.getTitle(), movie.getDescription()))
                .toList();

        return new MovieListResponse(movieResponses);
    }
}
