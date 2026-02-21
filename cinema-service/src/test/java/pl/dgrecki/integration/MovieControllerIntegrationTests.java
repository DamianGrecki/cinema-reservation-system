package pl.dgrecki.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.dgrecki.constants.Endpoints.MOVIES_ENDPOINT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.dgrecki.models.responses.movie.MovieListResponse;
import pl.dgrecki.models.responses.movie.MovieResponse;
import pl.dgrecki.services.MovieService;

@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @SneakyThrows
    @Test
    void shouldReturnMoviesListTest() {
        MovieResponse movie1 = new MovieResponse(UUID.randomUUID(), "Movie 1", "Desc 1");
        MovieResponse movie2 = new MovieResponse(UUID.randomUUID(), "Movie 2", "Desc 2");
        MovieListResponse movieListResponse = new MovieListResponse(List.of(movie1, movie2));

        when(movieService.getMoviesList()).thenReturn(movieListResponse);

        MvcResult mvcResult =
                mockMvc.perform(get(MOVIES_ENDPOINT)).andExpect(status().isOk()).andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        MovieListResponse response = objectMapper.readValue(json, MovieListResponse.class);
        List<MovieResponse> movies = response.getMovies();

        assertEquals(2, movies.size());
        assertEquals(movie1.getId(), movies.getFirst().getId());
        assertEquals(movie1.getTitle(), movies.getFirst().getTitle());
        assertEquals(movie1.getDescription(), movies.getFirst().getDescription());

        assertEquals(movie2.getId(), movies.getLast().getId());
        assertEquals(movie2.getTitle(), movies.getLast().getTitle());
        assertEquals(movie2.getDescription(), movies.getLast().getDescription());
    }
}
